package bioCanteenApp.authentication.controller;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.service.IAuthenticationService;
import bioCanteenApp.users.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<UserDTO> login(@RequestBody LoginDTO dto) {
        LoginResponse loginResponse = authenticationService.login(dto);
        if (loginResponse == null) return ResponseEntity.status(401).build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, loginResponse.getTokenType() + " " + loginResponse.getToken());

        UserDTO user = loginResponse.getUser();
        if (user != null) {
            user.setPassword(null);
        }

        return ResponseEntity.ok().headers(headers).body(user);
    }
}