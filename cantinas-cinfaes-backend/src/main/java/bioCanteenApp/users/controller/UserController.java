package bioCanteenApp.users.controller;

import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.users.service.IUserService;
import bioCanteenApp.utils.exceptions.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<List<GetUserDTO>> getAllUsers() {

        log.info("Fetching all users");

        List<GetUserDTO> users =
                userService.getAllUsers();

        log.info("Found {} users", users.size());

        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(
            @RequestBody UserDTO dto
    ) {

        log.info(
                "Creating user with email: {} and role: {}",
                LogSanitizer.sanitize(dto.getEmail()),
                LogSanitizer.sanitize(dto.getRole())
        );

        UserDTO createdUser =
                userService.createUser(dto);

        log.info(
                "User created successfully with id: {}",
                createdUser.getId()
        );

        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<GetUserDTO> getUserByEmail(
            @PathVariable("email") String email
    ) {

        log.info(
                "Fetching user with email: {}",
                LogSanitizer.sanitize(email)
        );

        GetUserDTO userDTO =
                userService.getUserByEmail(email);

        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("email") String email
    ) {

        log.warn(
                "Deleting user with email: {}",
                LogSanitizer.sanitize(email)
        );

        userService.deleteUser(email);

        log.info(
                "User deleted successfully with email: {}",
                LogSanitizer.sanitize(email)
        );

        return ResponseEntity.noContent().build();
    }
}