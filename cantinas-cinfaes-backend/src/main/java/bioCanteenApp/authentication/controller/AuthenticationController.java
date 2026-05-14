package bioCanteenApp.authentication.controller;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.service.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginDTO dto) {
        LoginResponse loginResponse = authenticationService.login(dto);
        if (loginResponse == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(loginResponse);
    }
}