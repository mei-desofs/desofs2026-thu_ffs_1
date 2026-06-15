package bioCanteenApp.security.controller;

import bioCanteenApp.security.dto.ForgotPasswordDTO;
import bioCanteenApp.security.service.PasswordService;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/passwords")
public class PasswordController {

    private static final Logger log = LoggerFactory.getLogger(PasswordController.class);

    private final PasswordService passwordService;
    private final UserRepo userRepository;

    public PasswordController(PasswordService passwordService, UserRepo userRepository) {
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/change", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> payload
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {

            log.warn("Unauthorized password change attempt");

            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();

        log.info("Password change requested for user: {}", username);

        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        if (currentPassword == null || newPassword == null) {

            log.warn(
                    "Password change request failed due to missing fields for user: {}",
                    username
            );

            return ResponseEntity.badRequest()
                    .body("Both 'currentPassword' and 'newPassword' are required.");
        }

        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordService.isPasswordExpired(user)) {
            log.warn(
                    "Password for user {} is expired; allowing change via API endpoint.",
                    user.getEmail()
            );
        }

        passwordService.changePassword(
                user,
                currentPassword,
                newPassword
        );

        log.info(
                "Password changed successfully for user: {}",
                username
        );

        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping(value = "/recover-password", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> forgotPassword(
            @RequestBody ForgotPasswordDTO payload
    ) {

        String email = payload.getEmail();

        log.info(
                "Password recovery requested for email: {}",
                email
        );

        passwordService.sendPasswordResetEmail(email);

        log.info(
                "Password recovery email processed for email: {}",
                email
        );

        return ResponseEntity.ok(
                "If an account with that email exists, a reset link has been sent. It will be valid for 20 minutes."
        );
    }

    @PostMapping(value = "/reset-password", produces =   MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resetPassword(
            @RequestBody Map<String, String> payload
    ) {

        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        log.info("Password reset attempt using reset token");

        if (token == null || newPassword == null) {

            log.warn("Password reset request failed due to missing fields");

            return ResponseEntity.badRequest()
                    .body("Both 'token' and 'newPassword' are required.");
        }

        passwordService.resetPasswordWithToken(
                token,
                newPassword
        );

        log.info("Password reset completed successfully");

        return ResponseEntity.ok(
                "Password reset successfully. You can now log in."
        );
    }

    @GetMapping(value = "/expired", produces =    MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isPasswordExpired(
            Authentication authentication
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {

            log.warn("Unauthorized password expiration check attempt");

            return ResponseEntity.status(401).build();
        }

        log.info(
                "Checking password expiration for user: {}",
                authentication.getName()
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        boolean expired =
                passwordService.isPasswordExpired(user);

        log.info(
                "Password expiration check completed for user: {}. Expired: {}",
                authentication.getName(),
                expired
        );

        return ResponseEntity.ok(expired);
    }
}