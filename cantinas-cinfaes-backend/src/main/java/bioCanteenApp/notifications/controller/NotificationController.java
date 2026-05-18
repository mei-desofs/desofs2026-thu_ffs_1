package bioCanteenApp.notifications.controller;

import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.notifications.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {

        log.info("Fetching all notifications");

        List<NotificationDTO> notifications =
                notificationService.getAllNotifications();

        log.info("Found {} notifications", notifications.size());

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(
            @PathVariable("id") Long id
    ) {

        log.info("Fetching notification with id: {}", id);

        NotificationDTO notification =
                notificationService.getById(id);

    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable("id") Long id) {
        NotificationDTO notification = notificationService.getById(id);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(
            @PathVariable("email") String email
    ) {

        log.info("Fetching notifications for user email: {}", email);

        List<NotificationDTO> notifications =
                notificationService.getByUserEmail(email);

        log.info(
                "Found {} notifications for user email: {}",
                notifications.size(),
                email
        );

        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(
            @RequestBody NotificationDTO dto
    ) {

        log.info(
                "Creating notification for user id: {}",
                dto.getUserId()
        );

        NotificationDTO createdNotification =
                notificationService.createNotification(dto);

        log.info(
                "Notification created successfully with id: {}",
                createdNotification.getId()
        );

        return ResponseEntity.ok(createdNotification);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") Long id
    ) {

        log.info("Marking notification as read with id: {}", id);

    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);

        log.info("Notification marked as read successfully with id: {}", id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/user/{userId}/read")
    public ResponseEntity<Void> markAllAsReadForUser(
            @PathVariable("userId") Long userId
    ) {

        log.info(
                "Marking all notifications as read for user id: {}",
                userId
        );

    public ResponseEntity<Void> markAllAsReadForUser(@PathVariable("userId") Long userId) {
        notificationService.markAllAsReadForUser(userId);

        log.info(
                "All notifications marked as read successfully for user id: {}",
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable("id") Long id
    ) {

        log.warn("Deleting notification with id: {}", id);

        notificationService.deleteById(id);

        log.info("Notification deleted successfully with id: {}", id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllNotificationsForUser(
            @PathVariable("userId") Long userId
    ) {

        log.warn(
                "Deleting all notifications for user id: {}",
                userId
        );

    public ResponseEntity<Void> deleteAllNotificationsForUser(@PathVariable("userId") Long userId) {
        notificationService.deleteAllForUser(userId);

        log.info(
                "All notifications deleted successfully for user id: {}",
                userId
        );

        return ResponseEntity.noContent().build();
    }
}