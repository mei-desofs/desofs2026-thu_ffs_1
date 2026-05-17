package bioCanteenApp.notifications.service;

import bioCanteenApp.notifications.domain.Notification;
import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.notifications.dto.NotificationType;
import bioCanteenApp.notifications.mappers.INotificationMapper;
import bioCanteenApp.notifications.repository.INotificationRepo;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private INotificationRepo repo;
    private INotificationMapper mapper;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        repo = mock(INotificationRepo.class);
        mapper = mock(INotificationMapper.class);

        service = new NotificationService(
                repo,
                mapper
        );
    }

    @Test
    void shouldCreateNotification() {
        Notification notification = createNotification();

        NotificationDTO dto = new NotificationDTO();

        when(mapper.toDomain(dto))
                .thenReturn(notification);

        when(repo.save(notification))
                .thenReturn(notification);

        when(mapper.toDTO(notification))
                .thenReturn(dto);

        NotificationDTO result =
                service.createNotification(dto);

        assertEquals(dto, result);

        verify(mapper).toDomain(dto);
        verify(repo).save(notification);
        verify(mapper).toDTO(notification);
    }

    @Test
    void shouldGetAllNotifications() {
        Notification notification1 = createNotification();
        Notification notification2 = createNotification();

        NotificationDTO dto1 = new NotificationDTO();
        NotificationDTO dto2 = new NotificationDTO();

        when(repo.findAll())
                .thenReturn(List.of(notification1, notification2));

        when(mapper.toDTO(notification1))
                .thenReturn(dto1);

        when(mapper.toDTO(notification2))
                .thenReturn(dto2);

        List<NotificationDTO> result =
                service.getAllNotifications();

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(repo).findAll();
        verify(mapper).toDTO(notification1);
        verify(mapper).toDTO(notification2);
    }

    @Test
    void shouldGetNotificationById() {
        Notification notification = createNotification();

        NotificationDTO dto = new NotificationDTO();

        when(repo.findById(1L))
                .thenReturn(Optional.of(notification));

        when(mapper.toDTO(notification))
                .thenReturn(dto);

        NotificationDTO result =
                service.getById(1L);

        assertEquals(dto, result);

        verify(repo).findById(1L);
        verify(mapper).toDTO(notification);
    }

    @Test
    void shouldThrowWhenNotificationNotFoundById() {
        when(repo.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.getById(1L)
        );

        verify(repo).findById(1L);
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void shouldGetNotificationsByUserEmail() {
        Notification notification1 = createNotification();
        Notification notification2 = createNotification();

        NotificationDTO dto1 = new NotificationDTO();
        NotificationDTO dto2 = new NotificationDTO();

        when(repo.findByUserEmail("user@email.com"))
                .thenReturn(List.of(notification1, notification2));

        when(mapper.toDTO(notification1))
                .thenReturn(dto1);

        when(mapper.toDTO(notification2))
                .thenReturn(dto2);

        List<NotificationDTO> result =
                service.getByUserEmail("user@email.com");

        assertEquals(2, result.size());

        verify(repo).findByUserEmail("user@email.com");
        verify(mapper).toDTO(notification1);
        verify(mapper).toDTO(notification2);
    }

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = createNotification();

        when(repo.findById(1L))
                .thenReturn(Optional.of(notification));

        service.markAsRead(1L);

        verify(repo).findById(1L);
        verify(repo).markAsRead(1L);
    }

    @Test
    void shouldThrowWhenMarkingNonExistingNotificationAsRead() {
        when(repo.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.markAsRead(1L)
        );

        verify(repo).findById(1L);
        verify(repo, never()).markAsRead(anyLong());
    }

    @Test
    void shouldMarkAllNotificationsAsReadForUser() {
        service.markAllAsReadForUser(1L);

        verify(repo).markAllAsReadForUser(1L);
    }

    @Test
    void shouldDeleteNotificationById() {
        service.deleteById(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void shouldDeleteAllNotificationsForUser() {
        service.deleteAllForUser(1L);

        verify(repo).deleteAllForUser(1L);
    }

    private Notification createNotification() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        return new Notification(
                user,
                "Important Notification",
                "Notification message",
                NotificationType.INFO,
                LocalDateTime.now(),
                false,
                "/notifications",
                1
        );
    }
}