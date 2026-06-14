package bioCanteenApp.notifications.controller;

import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.notifications.service.INotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private INotificationService notificationService;
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        notificationService = mock(INotificationService.class);

        controller = new NotificationController(notificationService);
    }

    @Test
    void shouldGetAllNotifications() {
        List<NotificationDTO> notifications = List.of(
                new NotificationDTO(),
                new NotificationDTO()
        );

        when(notificationService.getAllNotifications())
                .thenReturn(notifications);

        ResponseEntity<List<NotificationDTO>> response =
                controller.getAllNotifications();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(notifications, response.getBody());

        verify(notificationService).getAllNotifications();
    }

    @Test
    void shouldGetNotificationById() {
        NotificationDTO dto = new NotificationDTO();

        when(notificationService.getById(1L))
                .thenReturn(dto);

        ResponseEntity<NotificationDTO> response =
                controller.getNotificationById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(notificationService).getById(1L);
    }

    @Test
    void shouldGetNotificationsByUser() {
        List<NotificationDTO> notifications = List.of(
                new NotificationDTO(),
                new NotificationDTO()
        );

        when(notificationService.getByUserEmail("user@email.com"))
                .thenReturn(notifications);

        ResponseEntity<List<NotificationDTO>> response =
                controller.getNotificationsByUser("user@email.com");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(notifications, response.getBody());

        verify(notificationService)
                .getByUserEmail("user@email.com");
    }

    @Test
    void shouldCreateNotification() {
        NotificationDTO dto = new NotificationDTO();

        when(notificationService.createNotification(dto))
                .thenReturn(dto);

        ResponseEntity<NotificationDTO> response =
                controller.createNotification(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(notificationService).createNotification(dto);
    }

    @Test
    void shouldMarkAsRead() {
        ResponseEntity<Void> response =
                controller.markAsRead(1L);

        assertEquals(204, response.getStatusCode().value());

        verify(notificationService).markAsRead(1L);
    }

    @Test
    void shouldMarkAllAsReadForUser() {
        ResponseEntity<Void> response =
                controller.markAllAsReadForUser(1L);

        assertEquals(204, response.getStatusCode().value());

        verify(notificationService)
                .markAllAsReadForUser(1L);
    }

    @Test
    void shouldDeleteNotification() {
        ResponseEntity<Void> response =
                controller.deleteNotification(1L);

        assertEquals(204, response.getStatusCode().value());

        verify(notificationService).deleteById(1L);
    }

    @Test
    void shouldDeleteAllNotificationsForUser() {
        ResponseEntity<Void> response =
                controller.deleteAllNotificationsForUser(1L);

        assertEquals(204, response.getStatusCode().value());

        verify(notificationService).deleteAllForUser(1L);
    }
}