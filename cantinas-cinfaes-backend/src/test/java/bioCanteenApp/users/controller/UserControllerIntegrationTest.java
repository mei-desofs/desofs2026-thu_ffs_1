package bioCanteenApp.users.controller;

import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.users.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@AutoConfigureTestDatabase
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    private GetUserDTO mockGetUserDTO;
    private UserDTO mockUserDTO;

    @BeforeEach
    void setUp() {
        mockGetUserDTO = new GetUserDTO();
        mockUserDTO = new UserDTO();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        List<GetUserDTO> usersList = Arrays.asList(mockGetUserDTO);

        when(userService.getAllUsers()).thenReturn(usersList);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_AsAdmin_ShouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(mockUserDTO);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_AsRegularUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUserDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserByEmail_ShouldReturnUser() throws Exception {
        String email = "teste@bio.pt";
        when(userService.getUserByEmail(email)).thenReturn(mockGetUserDTO);

        mockMvc.perform(get("/api/users/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturnNoContent() throws Exception {
        String emailToDelete = "eliminar@bio.pt";
        doNothing().when(userService).deleteUser(emailToDelete);

        mockMvc.perform(delete("/api/users/{email}", emailToDelete)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}