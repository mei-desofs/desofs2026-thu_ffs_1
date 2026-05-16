package bioCanteenApp.authentication.controller;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.service.IAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest {

    private IAuthenticationService authenticationService;
    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        authenticationService = mock(IAuthenticationService.class);

        controller = new AuthenticationController(authenticationService);
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@email.com");
        dto.setPassword("password");

        LoginResponse responseDto = LoginResponse.builder()
                .token("JWT Token")
                .tokenType("Bearer")
                .build();

        when(authenticationService.login(dto))
                .thenReturn(responseDto);

        ResponseEntity<LoginResponse> response =
                controller.login(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseDto, response.getBody());

        verify(authenticationService).login(dto);
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@email.com");
        dto.setPassword("wrong-password");

        when(authenticationService.login(dto))
                .thenReturn(null);

        ResponseEntity<LoginResponse> response =
                controller.login(dto);

        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(authenticationService).login(dto);
    }
}