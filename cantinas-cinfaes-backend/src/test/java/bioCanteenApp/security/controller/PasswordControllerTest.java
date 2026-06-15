package bioCanteenApp.security.controller;

import bioCanteenApp.security.dto.ForgotPasswordDTO;
import bioCanteenApp.security.service.PasswordService;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordControllerTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private PasswordService passwordService;
    private UserRepo userRepository;
    private PasswordController controller;

    @BeforeEach
    void setUp() {
        passwordService = mock(PasswordService.class);
        userRepository = mock(UserRepo.class);

        controller = new PasswordController(
                passwordService,
                userRepository
        );
    }

    @Test
    void shouldChangePassword() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        when(authentication.getName())
                .thenReturn("user@email.com");

        when(userRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordService.isPasswordExpired(user))
                .thenReturn(false);

        ResponseEntity<String> response = controller.changePassword(
                authentication,
                Map.of(
                        "currentPassword", "oldPassword",
                        "newPassword", "newPassword"
                )
        );

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Password changed successfully.", response.getBody());

        verify(passwordService).changePassword(
                user,
                "oldPassword",
                "newPassword"
        );
    }

    @Test
    void shouldReturnBadRequestWhenCurrentPasswordIsMissing() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        ResponseEntity<String> response = controller.changePassword(
                authentication,
                Map.of("newPassword", "newPassword")
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Both 'currentPassword' and 'newPassword' are required.",
                response.getBody()
        );

        verify(passwordService, never())
                .changePassword(any(), any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenNewPasswordIsMissing() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        ResponseEntity<String> response = controller.changePassword(
                authentication,
                Map.of("currentPassword", "oldPassword")
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Both 'currentPassword' and 'newPassword' are required.",
                response.getBody()
        );

        verify(passwordService, never())
                .changePassword(any(), any(), any());
    }

    @Test
    void shouldThrowWhenUserNotFoundOnChangePassword() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        when(authentication.getName())
                .thenReturn("missing@email.com");

        when(userRepository.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> controller.changePassword(
                        authentication,
                        Map.of(
                                "currentPassword", "oldPassword",
                                "newPassword", "newPassword"
                        )
                )
        );

        verify(passwordService, never())
                .changePassword(any(), any(), any());
    }

    @Test
    void shouldRecoverPassword() {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setEmail("user@email.com");

        ResponseEntity<String> response =
                controller.forgotPassword(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(
                "If an account with that email exists, a reset link has been sent. It will be valid for 20 minutes.",
                response.getBody()
        );

        verify(passwordService)
                .sendPasswordResetEmail("user@email.com");
    }

    @Test
    void shouldResetPassword() {
        ResponseEntity<String> response =
                controller.resetPassword(
                        Map.of(
                                "token", "token123",
                                "newPassword", "newPassword"
                        )
                );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(
                "Password reset successfully. You can now log in.",
                response.getBody()
        );

        verify(passwordService)
                .resetPasswordWithToken(
                        "token123",
                        "newPassword"
                );
    }

    @Test
    void shouldReturnBadRequestWhenTokenIsMissing() {
        ResponseEntity<String> response =
                controller.resetPassword(
                        Map.of("newPassword", "newPassword")
                );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Both 'token' and 'newPassword' are required.",
                response.getBody()
        );

        verify(passwordService, never())
                .resetPasswordWithToken(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenNewPasswordIsMissingOnReset() {
        ResponseEntity<String> response =
                controller.resetPassword(
                        Map.of("token", "token123")
                );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Both 'token' and 'newPassword' are required.",
                response.getBody()
        );

        verify(passwordService, never())
                .resetPasswordWithToken(any(), any());
    }

    @Test
    void shouldCheckIfPasswordIsExpired() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        when(authentication.getName())
                .thenReturn("user@email.com");

        when(userRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordService.isPasswordExpired(user))
                .thenReturn(true);

        ResponseEntity<Boolean> response =
                controller.isPasswordExpired(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(Boolean.TRUE, response.getBody());

        verify(passwordService).isPasswordExpired(user);
    }

    @Test
    void shouldThrowWhenUserNotFoundOnPasswordExpired() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);

        when(authentication.getName())
                .thenReturn("missing@email.com");

        when(userRepository.findByEmail("missing@email.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.isPasswordExpired(authentication)
        );

        verify(passwordService, never())
                .isPasswordExpired(any());
    }
}