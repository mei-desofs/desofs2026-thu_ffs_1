package bioCanteenApp.notifications.service;

import bioCanteenApp.notifications.dto.NotificationDTO;

import java.util.List;

public interface INotificationService {
    NotificationDTO createNotification(NotificationDTO dto);
    List<NotificationDTO> getAllNotifications();
    NotificationDTO getById(Long id);
    List<NotificationDTO> getByUserEmail(String userId);
    void markAsRead(Long id);
    void markAllAsReadForUser(Long userId);
    void deleteById(Long id);
    void deleteAllForUser(Long userId);
}
