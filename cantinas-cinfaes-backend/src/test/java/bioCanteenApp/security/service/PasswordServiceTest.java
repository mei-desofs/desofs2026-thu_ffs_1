package bioCanteenApp.security.service;

import bioCanteenApp.email.service.EmailService;
import bioCanteenApp.security.domain.PasswordHistory;
import bioCanteenApp.security.domain.PasswordResetToken;
import bioCanteenApp.security.repository.PasswordHistoryRepo;
import bioCanteenApp.security.repository.PasswordResetTokenRepo;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordServiceTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private PasswordHistoryRepo passwordHistoryRepo;
    private PasswordResetTokenRepo passwordResetTokenRepo;
    private UserRepo userRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;

    private PasswordService service;

    @BeforeEach
    void setUp() {
        passwordHistoryRepo = mock(PasswordHistoryRepo.class);
        passwordResetTokenRepo = mock(PasswordResetTokenRepo.class);
        userRepository = mock(UserRepo.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);

        service = new PasswordService(
                passwordHistoryRepo,
                passwordResetTokenRepo,
                userRepository,
                passwordEncoder,
                emailService
        );
    }

    @Test
    void shouldValidateStrongPassword() {
        assertDoesNotThrow(() ->
                service.validatePasswordStrength("StrongPass1!")
        );
    }

    @Test
    void shouldThrowWhenPasswordIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validatePasswordStrength(null)
        );
    }

    @Test
    void shouldThrowWhenPasswordIsCommon() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validatePasswordStrength("password")
        );
    }

    @Test
    void shouldThrowWhenPasswordIsWeak() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validatePasswordStrength("weakpass")
        );
    }

    @Test
    void shouldValidateHistoricalPasswords() {
        User user = new User("user@email.com", "User", "password");

        when(passwordHistoryRepo.findTop5ByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of());

        assertDoesNotThrow(() ->
                service.validateHistoricalPasswords(user, "StrongPass1!")
        );
    }

    @Test
    void shouldThrowWhenPasswordWasAlreadyUsed() {
        User user = new User("user@email.com", "User", "password");

        PasswordHistory history = new PasswordHistory(user, "StrongPass1!");

        when(passwordHistoryRepo.findTop5ByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(history));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateHistoricalPasswords(user, "StrongPass1!")
        );
    }

    @Test
    void shouldChangePassword() {
        User user = new User("user@email.com", "User", "oldPassword");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "oldPassword"))
                .thenReturn(true);
        when(passwordHistoryRepo.findTop5ByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of());
        when(passwordEncoder.encode("StrongPass1!"))
                .thenReturn("encodedPassword");

        service.changePassword(user, "oldPassword", "StrongPass1!");

        assertEquals("StrongPass1!", user.getPassword());
        assertNotNull(user.getPasswordChangedAt());

        verify(passwordHistoryRepo).save(any(PasswordHistory.class));
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenUserNotFoundOnChangePassword() {
        User user = new User("user@email.com", "User", "oldPassword");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(user, "oldPassword", "StrongPass1!")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCurrentPasswordIsIncorrect() {
        User user = new User("user@email.com", "User", "oldPassword");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "oldPassword"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changePassword(user, "wrongPassword", "StrongPass1!")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldApplyNewPassword() {
        User user = new User("user@email.com", "User", "oldPassword");

        when(passwordEncoder.encode("StrongPass1!"))
                .thenReturn("encodedPassword");

        service.applyNewPassword(user, "StrongPass1!");

        assertEquals("StrongPass1!", user.getPassword());
        assertNotNull(user.getPasswordChangedAt());

        verify(passwordHistoryRepo).save(any(PasswordHistory.class));
        verify(userRepository).save(user);
    }

    @Test
    void shouldReturnTrueWhenPasswordChangedAtIsNull() {
        User user = new User("user@email.com", "User", "password");

        assertTrue(service.isPasswordExpired(user));
    }

    @Test
    void shouldReturnTrueWhenPasswordIsOlderThanSixMonths() {
        User user = new User("user@email.com", "User", "password");
        user.setPasswordChangedAt(LocalDateTime.now().minusMonths(7));

        assertTrue(service.isPasswordExpired(user));
    }

    @Test
    void shouldReturnFalseWhenPasswordIsRecent() {
        User user = new User("user@email.com", "User", "password");
        user.setPasswordChangedAt(LocalDateTime.now().minusMonths(2));

        assertFalse(service.isPasswordExpired(user));
    }

    @Test
    void shouldSendPasswordResetEmail() {
        User user = new User("user@email.com", "User", "password");
        user.setId(1L);

        when(userRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        service.sendPasswordResetEmail("user@email.com");

        verify(passwordResetTokenRepo).deleteAllByUserId(1L);
        verify(passwordResetTokenRepo).save(any(PasswordResetToken.class));
        verify(emailService).sendEmail(
                eq("user@email.com"),
                contains("reset-password?token=")
        );
    }

    @Test
    void shouldThrowWhenEmailDoesNotExistOnResetEmail() {
        when(userRepository.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendPasswordResetEmail("missing@email.com")
        );

        verify(emailService, never()).sendEmail(any(), any());
    }
}