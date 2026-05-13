package bioCanteenApp.notifications.controller;

import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.notifications.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final INotificationService notificationService;

    // GET all notifications
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    // GET notification by ID
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        NotificationDTO notification = notificationService.getById(id);
        return ResponseEntity.ok(notification);
    }

    // GET notifications by User ID
    @GetMapping("/user/{email}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(@PathVariable("email") String email) {
        List<NotificationDTO> notifications = notificationService.getByUserEmail(email);
        return ResponseEntity.ok(notifications);
    }

    // POST create notification
    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@RequestBody NotificationDTO dto) {
        NotificationDTO createdNotification = notificationService.createNotification(dto);
        return ResponseEntity.ok(createdNotification);
    }

    // PUT mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    // PUT mark all notifications as read for a user
    @PutMapping("/user/{userId}/read")
    public ResponseEntity<Void> markAllAsReadForUser(@PathVariable Long userId) {
        notificationService.markAllAsReadForUser(userId);
        return ResponseEntity.noContent().build();
    }

    // DELETE notification by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable("id") Long id) {
        notificationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE all notifications for a user
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllNotificationsForUser(@PathVariable Long userId) {
        notificationService.deleteAllForUser(userId);
        return ResponseEntity.noContent().build();
    }
}
