package bioCanteenApp.notifications.repository;

import bioCanteenApp.notifications.domain.Notification;

import java.util.List;
import java.util.Optional;

public interface INotificationRepo {
    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findAll();

    List<Notification> findByUserEmail(String userId);

    List<Notification> findUnreadByUserId(Long userId);

    void markAsRead(Long id);

    void markAllAsReadForUser(Long userId);

    void deleteById(Long id);

    void deleteAllForUser(Long userId);
}
