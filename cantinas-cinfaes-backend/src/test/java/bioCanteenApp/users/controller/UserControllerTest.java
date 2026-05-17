package bioCanteenApp.users.controller;

import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.users.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private IUserService userService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userService = mock(IUserService.class);

        controller = new UserController(userService);
    }

    @Test
    void shouldGetAllUsers() {
        List<GetUserDTO> users = List.of(
                new GetUserDTO(),
                new GetUserDTO()
        );

        when(userService.getAllUsers())
                .thenReturn(users);

        ResponseEntity<List<GetUserDTO>> response =
                controller.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(users, response.getBody());

        verify(userService).getAllUsers();
    }

    @Test
    void shouldCreateUser() {
        UserDTO dto = new UserDTO();

        when(userService.createUser(dto))
                .thenReturn(dto);

        ResponseEntity<UserDTO> response =
                controller.createUser(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(userService).createUser(dto);
    }

    @Test
    void shouldGetUserByEmail() {
        GetUserDTO dto = new GetUserDTO();

        when(userService.getUserByEmail("user@email.com"))
                .thenReturn(dto);

        ResponseEntity<GetUserDTO> response =
                controller.getUserByEmail("user@email.com");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(userService)
                .getUserByEmail("user@email.com");
    }

    @Test
    void shouldDeleteUser() {
        ResponseEntity<Void> response =
                controller.deleteUser("user@email.com");

        assertEquals(204, response.getStatusCode().value());

        verify(userService)
                .deleteUser("user@email.com");
    }
}