package bioCanteenApp.security.service;

import bioCanteenApp.security.domain.PasswordHistory;
import bioCanteenApp.security.repository.PasswordHistoryRepo;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PasswordService implements IPasswordService {

    private final PasswordHistoryRepo passwordHistoryRepo;
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

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
                .anyMatch(ph -> ph.getPassword().equals(newPassword));

        if (isReused) {
            throw new IllegalArgumentException("New password cannot be the same as any of the last 5 passwords.");
        }
    }

    @Override
    public void changePassword(User userId, String currentPassword, String newPassword) {

        User user = userRepository.findById(userId.getId());

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!user.getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        validatePasswordStrength(newPassword);
        validateHistoricalPasswords(userId, newPassword);

        applyNewPassword(user, newPassword);
    }

    @Override
    public void applyNewPassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);

        PasswordHistory passwordHistory = new PasswordHistory(user, encodedPassword);
        passwordHistoryRepo.save(passwordHistory);

        user.setPassword(newPassword);
        user.setPasswordChangedAt(LocalDateTime.now());
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


}
