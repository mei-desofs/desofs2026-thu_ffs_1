package bioCanteenApp.authentication.controller;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.service.IAuthenticationService;
import bioCanteenApp.users.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<UserDTO> login(@RequestBody LoginDTO dto) {

        log.info("Authentication attempt for email: {}", dto.getEmail());

        LoginResponse loginResponse =
                authenticationService.login(dto);

        if (loginResponse == null) {

            log.warn(
                    "Authentication failed for email: {}",
                    dto.getEmail()
            );

            return ResponseEntity.status(401).build();
        }

        HttpHeaders headers = new HttpHeaders();

        headers.add(
                HttpHeaders.AUTHORIZATION,
                loginResponse.getTokenType() + " " + loginResponse.getToken()
        );

        UserDTO user = loginResponse.getUser();

        if (user != null) {

            log.info(
                    "User authenticated successfully. User id: {}, email: {}, role: {}",
                    user.getId(),
                    user.getEmail(),
                    user.getRole()
            );

            user.setPassword(null);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserDTO> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        LoginResponse response = authenticationService.refreshToken(refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, response.getTokenType() + " " + response.getToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getUser());
    }
}