package bioCanteenApp.security.service;

import bioCanteenApp.email.service.EmailService;
import bioCanteenApp.security.domain.PasswordHistory;
import bioCanteenApp.security.domain.PasswordResetToken;
import bioCanteenApp.security.repository.PasswordHistoryRepo;
import bioCanteenApp.security.repository.PasswordResetTokenRepo;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService implements IPasswordService {

    private final PasswordHistoryRepo passwordHistoryRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password1", "123456", "12345678", "1234567890",
            "qwerty", "abc123", "111111", "letmein", "welcome",
            "monkey", "dragon", "master", "sunshine", "princess",
            "admin", "login", "pass", "test", "iloveyou"
    );

    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{10,}$";

    @Override
    public void validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            throw new IllegalArgumentException("Password is too common. Please choose a more secure password.");
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            throw new IllegalArgumentException("Password must be at least 10 characters long and include at least one uppercase letter, one number, and one special character.");
        }

    }

    @Override
    public void validateHistoricalPasswords(User userId, String newPassword) {
        List<PasswordHistory> passwordHistories = passwordHistoryRepo.findTop5ByUserOrderByCreatedAtDesc(userId);

        boolean isReused = passwordHistories.stream()
                .anyMatch(ph -> passwordEncoder.matches(newPassword, ph.getPassword()));

        if (isReused) {
            throw new IllegalArgumentException("New password cannot be the same as any of the last 5 passwords.");
        }
    }

    @Override
    @Transactional
    public void changePassword(User userId, String currentPassword, String newPassword) {

        User user = userRepository.findById(userId.getId());

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        validateNewPassword(user, newPassword);

        applyNewPassword(user, newPassword);
    }

    @Override
    @Transactional
    public void applyNewPassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);

        PasswordHistory passwordHistory = new PasswordHistory(user, encodedPassword);
        passwordHistoryRepo.save(passwordHistory);

        // store encoded password on the user
        user.setPassword(encodedPassword);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Override
    public boolean isPasswordExpired(User user) {
        LocalDateTime lastChanged = user.getPasswordChangedAt();
        if (lastChanged == null) {
            return true; // If never changed, consider it expired
        }
        return lastChanged.isBefore(LocalDateTime.now().minusMonths(6));
    }

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found for that email."));

        // Invalidate any existing tokens for this user
        passwordResetTokenRepo.deleteAllByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(user, rawToken);
        passwordResetTokenRepo.save(resetToken);

        // REQ2.3: send email with link (token valid 20 min — set inside constructor)
        String resetLink = "https://biocantinas.app/reset-password?token=" + rawToken;
        emailService.sendEmail(user.getEmail(), resetLink);
    }

    @Override
    public String generateSupplierSetupToken(User user) {
        // 1. Limpa os tokens antigos e força a atualização na BD
        passwordResetTokenRepo.deleteAllByUserId(user.getId());

        // 2. Gera o token novo
        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(user, rawToken);

        // 3. Guarda na BD e força a escrita imediata
        passwordResetTokenRepo.save(resetToken);

        return rawToken;
    }

    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("This reset link has already been used.");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException(
                    "Reset link has expired. Please request a new one (valid for 20 minutes).");
        }

        User user = resetToken.getUser();

        validatePasswordStrength(newPassword);
        validateHistoricalPasswords(user, newPassword);

        applyNewPassword(user, newPassword);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepo.save(resetToken);
    }

    @Transactional
    public void sendSupplierActivationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found for that email."));

        passwordResetTokenRepo.deleteAllByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(user, rawToken);
        passwordResetTokenRepo.save(resetToken);

        // Manda o token em plain text para usar no Postman
        emailService.sendSupplierWelcomeEmail(user.getEmail(), rawToken);
    }

    private void validateNewPassword(User user, String newPassword) {

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be same as current password.");
        }

        validatePasswordStrength(newPassword);
        validateHistoricalPasswords(user, newPassword);
    }
}