package bioCanteenApp.notifications.controller;

import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.notifications.service.INotificationService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private INotificationService notificationService;

    private NotificationDTO notificationDTO;

    @BeforeEach
    void setUp() {
        notificationDTO = NotificationDTO.builder()
                .id(1L)
                .userId(10L)
                .title("Test Notification")
                .message("This is a test")
                .build();
    }

    @Test
    @WithMockUser
    void getAllNotifications_Authenticated_ShouldReturnOk() throws Exception {
        when(notificationService.getAllNotifications()).thenReturn(List.of(notificationDTO));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Notification"));
    }

    @Test
    void getAllNotifications_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getNotificationById_Authenticated_ShouldReturnOk() throws Exception {
        when(notificationService.getById(1L)).thenReturn(notificationDTO);

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void getNotificationsByUser_Authenticated_ShouldReturnOk() throws Exception {
        when(notificationService.getByUserEmail("user@gmail.com"))
                .thenReturn(List.of(notificationDTO));

        mockMvc.perform(get("/api/notifications/user/user@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void createNotification_Authenticated_ShouldReturnOk() throws Exception {
        when(notificationService.createNotification(any(NotificationDTO.class)))
                .thenReturn(notificationDTO);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void markAsRead_Authenticated_ShouldReturnNoContent() throws Exception {
        doNothing().when(notificationService).markAsRead(1L);

        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void markAllAsReadForUser_Authenticated_ShouldReturnNoContent() throws Exception {
        doNothing().when(notificationService).markAllAsReadForUser(10L);

        mockMvc.perform(put("/api/notifications/user/10/read"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteNotification_Authenticated_ShouldReturnNoContent() throws Exception {
        doNothing().when(notificationService).deleteById(1L);

        mockMvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteAllNotificationsForUser_Authenticated_ShouldReturnNoContent() throws Exception {
        doNothing().when(notificationService).deleteAllForUser(10L);

        mockMvc.perform(delete("/api/notifications/user/10"))
                .andExpect(status().isNoContent());
    }
}
