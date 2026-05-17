package bioCanteenApp.authentication;

import bioCanteenApp.authentication.dto.LoginDTO;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.IUserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        if (userRepo.findByEmail("integration@test.com").isEmpty()) {
            User user = new User(
                    "integration@test.com",
                    "Test User",
                    passwordEncoder.encode("SecureP@ssw0rd"),
                    Role.USER
            );
            userRepo.save(user);
        }
    }

    @Test
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("integration@test.com");
        loginDTO.setPassword("SecureP@ssw0rd");

        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
    }

    @Test
    void shouldFailLoginWithIncorrectPassword() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("integration@test.com");
        loginDTO.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoginWithNonExistentUser() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("unknown@test.com");
        loginDTO.setPassword("SecureP@ssw0rd");

        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }
}
