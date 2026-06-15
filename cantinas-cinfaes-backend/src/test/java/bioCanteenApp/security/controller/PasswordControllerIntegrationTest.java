package bioCanteenApp.security.controller;

import bioCanteenApp.security.dto.ForgotPasswordDTO;
import bioCanteenApp.security.service.PasswordService;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.UserRepo;
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

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class PasswordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordService passwordService;

    @Autowired
    private UserRepo userRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("user@email.com").isEmpty()) {
            mockUser = new User("user@email.com", "User Test", "password", Role.USER);
            userRepository.save(mockUser);
        } else {
            mockUser = userRepository.findByEmail("user@email.com").get();
        }
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldChangePasswordWhenAuthenticatedAndPayloadIsValid() throws Exception {
        Map<String, String> payload = Map.of(
                "currentPassword", "oldPass123",
                "newPassword", "newPass123"
        );

        when(passwordService.isPasswordExpired(any(User.class))).thenReturn(false);
        doNothing().when(passwordService).changePassword(any(User.class), eq("oldPass123"), eq("newPass123"));

        mockMvc.perform(post("/api/passwords/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully."));

        verify(passwordService).changePassword(any(User.class), eq("oldPass123"), eq("newPass123"));
    }

    @Test
    void shouldReturn401OnChangePasswordWhenNotAuthenticated() throws Exception {
        Map<String, String> payload = Map.of(
                "currentPassword", "oldPass123",
                "newPassword", "newPass123"
        );

        mockMvc.perform(post("/api/passwords/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldReturn400OnChangePasswordWhenPayloadIsMissingFields() throws Exception {
        Map<String, String> incompletePayload = Map.of(
                "currentPassword", "oldPass123"
        );

        mockMvc.perform(post("/api/passwords/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompletePayload)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Both 'currentPassword' and 'newPassword' are required."));
    }

    @Test
    void shouldSendRecoveryEmail() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setEmail("user@email.com");

        doNothing().when(passwordService).sendPasswordResetEmail("user@email.com");

        mockMvc.perform(post("/api/passwords/recover-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("If an account with that email exists, a reset link has been sent. It will be valid for 20 minutes."));

        verify(passwordService).sendPasswordResetEmail("user@email.com");
    }

    @Test
    void shouldActivateAccountdWithValidTokenAndPayload() throws Exception {
        mockMvc.perform(post("/api/passwords/activate-account")
                        .param("token", "valid-token-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"brandNewPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account activated successfully. You can now log in."));
    }

    @Test
    void shouldReturn400OnActivateAccountWhenTokenOrPasswordIsMissing() throws Exception {
        mockMvc.perform(post("/api/passwords/activate-account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"brandNewPassword\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldCheckIfPasswordIsExpiredWhenAuthenticated() throws Exception {
        when(passwordService.isPasswordExpired(any(User.class))).thenReturn(true);

        mockMvc.perform(get("/api/passwords/expired"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturn401OnExpiredCheckWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/passwords/expired"))
                .andExpect(status().isUnauthorized());
    }
}