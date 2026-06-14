package bioCanteenApp.notifications.domain;

import bioCanteenApp.notifications.dto.NotificationType;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void shouldCreateEmptyNotification() {
        Notification notification = new Notification();

        assertNull(notification.getId());
        assertNull(notification.getUser());
        assertNull(notification.getTitle());
        assertNull(notification.getMessage());
        assertNull(notification.getType());
        assertNull(notification.getCreatedAt());
        assertFalse(notification.isRead());
        assertNull(notification.getActionUrl());
        assertNull(notification.getPriority());
    }

    @Test
    void shouldCreateNotificationWithConstructor() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        LocalDateTime createdAt = LocalDateTime.now();

        Notification notification = new Notification(
                user,
                "New Notification",
                "This is a notification message",
                NotificationType.INFO,
                createdAt,
                true,
                "/notifications/1",
                1
        );

        assertEquals(user, notification.getUser());
        assertEquals("New Notification", notification.getTitle());
        assertEquals("This is a notification message", notification.getMessage());
        assertEquals(NotificationType.INFO, notification.getType());
        assertEquals(createdAt, notification.getCreatedAt());
        assertTrue(notification.isRead());
        assertEquals("/notifications/1", notification.getActionUrl());
        assertEquals(1, notification.getPriority());
    }

    @Test
    void shouldSetAndGetId() {
        Notification notification = new Notification();

        notification.setId(1L);

        assertEquals(1L, notification.getId());
    }

    @Test
    void shouldSetAndGetUser() {
        Notification notification = new Notification();

        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        notification.setUser(user);

        assertEquals(user, notification.getUser());
    }

    @Test
    void shouldSetAndGetTitle() {
        Notification notification = new Notification();

        notification.setTitle("Important");

        assertEquals("Important", notification.getTitle());
    }

    @Test
    void shouldSetAndGetMessage() {
        Notification notification = new Notification();

        notification.setMessage("Message content");

        assertEquals("Message content", notification.getMessage());
    }

    @Test
    void shouldSetAndGetType() {
        Notification notification = new Notification();

        notification.setType(NotificationType.WARNING);

        assertEquals(NotificationType.WARNING, notification.getType());
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        Notification notification = new Notification();

        LocalDateTime createdAt = LocalDateTime.now();

        notification.setCreatedAt(createdAt);

        assertEquals(createdAt, notification.getCreatedAt());
    }

    @Test
    void shouldSetAndGetRead() {
        Notification notification = new Notification();

        notification.setRead(true);

        assertTrue(notification.isRead());
    }

    @Test
    void shouldSetAndGetActionUrl() {
        Notification notification = new Notification();

        notification.setActionUrl("/notifications");

        assertEquals("/notifications", notification.getActionUrl());
    }

    @Test
    void shouldSetAndGetPriority() {
        Notification notification = new Notification();

        notification.setPriority(5);

        assertEquals(5, notification.getPriority());
    }
}