package bioCanteenApp.security.controller;

import bioCanteenApp.security.dto.ForgotPasswordDTO;
import bioCanteenApp.security.service.PasswordService;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
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

    @PostMapping("/change")
    public ResponseEntity<String> changePassword(Authentication authentication,
                                                 @RequestBody Map<String, String> payload) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();

        String currentPassword = payload.get("currentPassword");
        String newPassword     = payload.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Both 'currentPassword' and 'newPassword' are required.");
        }

        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // REQ2.5: inform user if password is already expired (handled by filter too)
        if (passwordService.isPasswordExpired(user)) {
            log.warn("Password for user {} is expired; allowing change via API endpoint.", user.getEmail());
        }

        passwordService.changePassword(user, currentPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping("/recover-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDTO payload) {

        String email = payload.getEmail();

        passwordService.sendPasswordResetEmail(email);

        return ResponseEntity.ok(
                "If an account with that email exists, a reset link has been sent. It will be valid for 20 minutes.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> payload) {
        String token       = payload.get("token");
        String newPassword = payload.get("newPassword");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body("Both 'token' and 'newPassword' are required.");
        }

        passwordService.resetPasswordWithToken(token, newPassword);
        return ResponseEntity.ok("Password reset successfully. You can now log in.");
    }

    @GetMapping("/expired")
    public ResponseEntity<Boolean> isPasswordExpired(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return ResponseEntity.ok(passwordService.isPasswordExpired(user));
    }
}
