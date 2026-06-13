package bioCanteenApp.notifications.mapper;

import bioCanteenApp.notifications.domain.Notification;
import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.notifications.dto.NotificationType;
import bioCanteenApp.notifications.mappers.NotificationMapper;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.IUserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationMapperTest {

    @Mock
    private IUserRepo userRepo;

    @InjectMocks
    private NotificationMapper mapper;

    @Test
    void toDTO_withNull_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_mapsAllFields() {
        User user = new User("user@test.com", "Test User", "password");
        user.setId(1L);

        Notification notification = new Notification(user, "Title", "Message",
                NotificationType.INFO, LocalDateTime.of(2026, 1, 1, 10, 0),
                false, "/action", 1);
        notification.setId(5L);

        NotificationDTO dto = mapper.toDTO(notification);

        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals(1L, dto.getUserId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Message", dto.getMessage());
        assertEquals(NotificationType.INFO, dto.getType());
        assertFalse(dto.isRead());
        assertEquals("/action", dto.getActionUrl());
        assertEquals(1, dto.getPriority());
    }

    @Test
    void toDTO_withNullUser_mapsNullUserId() {
        Notification notification = new Notification(null, "T", "M",
                NotificationType.ALERT, LocalDateTime.now(), true, null, null);

        NotificationDTO dto = mapper.toDTO(notification);

        assertNull(dto.getUserId());
    }

    @Test
    void toDomain_withNull_returnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_withNullUserId_createsNotificationWithNullUser() {
        NotificationDTO dto = NotificationDTO.builder()
                .userId(null)
                .title("Test")
                .message("Msg")
                .type(NotificationType.ALERT)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        Notification notification = mapper.toDomain(dto);

        assertNotNull(notification);
        assertEquals("Test", notification.getTitle());
        assertNull(notification.getUser());
    }

    @Test
    void toDomain_withUserId_loadsUserFromRepo() {
        User user = new User("u@test.com", "User", "pass");
        when(userRepo.findById(1L)).thenReturn(user);

        NotificationDTO dto = NotificationDTO.builder()
                .userId(1L)
                .title("Test")
                .message("Msg")
                .type(NotificationType.INFO)
                .createdAt(LocalDateTime.now())
                .isRead(true)
                .build();

        Notification notification = mapper.toDomain(dto);

        assertEquals(user, notification.getUser());
        verify(userRepo).findById(1L);
    }

    @Test
    void toDomain_withNullCreatedAt_usesCurrentTime() {
        NotificationDTO dto = NotificationDTO.builder()
                .userId(null)
                .title("T")
                .message("M")
                .type(NotificationType.INFO)
                .createdAt(null)
                .build();

        Notification notification = mapper.toDomain(dto);

        assertNotNull(notification.getCreatedAt());
    }
}
