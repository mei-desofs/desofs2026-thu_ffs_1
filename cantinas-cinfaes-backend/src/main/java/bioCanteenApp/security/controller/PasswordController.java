package bioCanteenApp.security.controller;

import bioCanteenApp.security.service.PasswordService;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/passwords")
public class PasswordController {

    private final PasswordService passwordService;
    private final UserRepo userRepository;

    public PasswordController(PasswordService passwordService, UserRepo userRepository) {
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }

    @PostMapping("/change")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestBody Map<String, String> payload) {

        String currentPassword = payload.get("currentPassword");
        String newPassword     = payload.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Both 'currentPassword' and 'newPassword' are required.").getBody();}

        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // REQ2.5: inform user if password is already expired (handled by filter too)
        if (passwordService.isPasswordExpired(user)) {
            // We still allow the change — this endpoint IS the solution
        }

        passwordService.changePassword(user, currentPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully.").getBody();
    }
}
