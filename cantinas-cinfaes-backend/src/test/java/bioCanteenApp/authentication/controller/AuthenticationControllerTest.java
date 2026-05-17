package bioCanteenApp.authentication.controller;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.service.IAuthenticationService;
import bioCanteenApp.users.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
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

        UserDTO userDto = new UserDTO();
        userDto.setEmail("user@email.com");

        LoginResponse responseDto = LoginResponse.builder()
                .token("JWT Token")
                .tokenType("Bearer")
                .user(userDto)
                .build();

        when(authenticationService.login(dto))
                .thenReturn(responseDto);

        ResponseEntity<UserDTO> response =
                controller.login(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Bearer JWT Token", response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(userDto, response.getBody());

        verify(authenticationService).login(dto);
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@email.com");
        dto.setPassword("wrong-password");

        when(authenticationService.login(dto))
                .thenReturn(null);

        ResponseEntity<UserDTO> response =
                controller.login(dto);

        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(authenticationService).login(dto);
    }
}