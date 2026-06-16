package bioCanteenApp.authentication.controller;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.service.IAuthenticationService;
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.utils.exceptions.LogSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> login(@RequestBody LoginDTO dto,
                                         HttpServletRequest request) {

        log.info("Authentication attempt for email: {}", dto.getEmail());

        // REQ1.4 — usa User-Agent se deviceId não vier no body
        if (dto.getDeviceId() == null || dto.getDeviceId().isBlank()) {
            dto.setDeviceId(request.getHeader("User-Agent"));
        }

        LoginResponse loginResponse =
                authenticationService.login(dto);

        if (loginResponse == null) {

            log.warn(
                    "Authentication failed for email: {}",
                    LogSanitizer.sanitize(dto.getEmail())
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
                    LogSanitizer.sanitize(user.getEmail()),
                    LogSanitizer.sanitize(user.getRole())
            );

            user.setPassword(null);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(user);
    }

    @PostMapping(value = "/refresh", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        LoginResponse response = authenticationService.refreshToken(refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, response.getTokenType() + " " + response.getToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getUser());
    }
}