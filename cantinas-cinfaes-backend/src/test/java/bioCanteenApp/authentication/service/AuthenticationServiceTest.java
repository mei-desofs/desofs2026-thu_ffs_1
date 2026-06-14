package bioCanteenApp.authentication.service;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.authentication.dto.LoginResponse;
import bioCanteenApp.authentication.exception.InvalidCredentialsException;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.users.mapper.UserMapper;
import bioCanteenApp.users.repository.IUserRepo;
import bioCanteenApp.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private IUserRepo userRepo;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldReturnNullWhenUserDoesNotExist() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@email.com");
        dto.setPassword("password");

        when(userRepo.findByEmail("user@email.com"))
                .thenReturn(Optional.empty());

        LoginResponse response = authenticationService.login(dto);

        assertNull(response);

        verify(userRepo).findByEmail("user@email.com");
        verifyNoInteractions(userMapper);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldReturnNullWhenPasswordIsWrong() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@email.com");
        dto.setPassword("wrongPassword");

        User user = new User(
                "user@email.com",
                "User",
                "encoded-password"
        );

        when(userRepo.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrongPassword", "encoded-password"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.login(dto);
        });

        verify(userRepo).findByEmail("user@email.com");
        verify(passwordEncoder).matches("wrongPassword", "encoded-password");
        verifyNoInteractions(userMapper);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldReturnLoginResponseWhenCredentialsAreValid() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@email.com");
        dto.setPassword("password");

        User user = new User(
                "user@email.com",
                "User",
                "encoded-password"
        );

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("user@email.com");
        userDTO.setName("User");

        when(userRepo.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);

        when(userMapper.toDTO(user))
                .thenReturn(userDTO);

        when(jwtService.generateToken(user))
                .thenReturn("JWT Token");

        LoginResponse response = authenticationService.login(dto);

        assertNotNull(response);
        assertEquals("JWT Token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(userDTO, response.getUser());

        verify(userRepo).findByEmail("user@email.com");
        verify(passwordEncoder).matches("password", "encoded-password");
        verify(userMapper).toDTO(user);
        verify(jwtService).generateToken(user);
    }
}