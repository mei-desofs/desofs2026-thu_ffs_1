package bioCanteenApp.security.controller;

import bioCanteenApp.security.service.PasswordService;
import bioCanteenApp.users.repository.UserRepo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/passwords")
public class PasswordController {

    private final PasswordService passwordService;
    private final UserRepo userRepository;

    public PasswordController(PasswordService passwordService, UserRepo userRepository) {
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }
}
