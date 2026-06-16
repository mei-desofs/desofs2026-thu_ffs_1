package bioCanteenApp.notifications.controller;

import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.notifications.service.INotificationService;
import bioCanteenApp.utils.exceptions.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {

        log.info("Fetching all notifications");

        List<NotificationDTO> notifications =
                notificationService.getAllNotifications();

        log.info("Found {} notifications", notifications.size());

        return ResponseEntity.ok(notifications);
    }

    @GetMapping(value = "/{id}", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NotificationDTO> getNotificationById(
            @PathVariable("id") Long id
    ) {

        log.info("Fetching notification with id: {}", LogSanitizer.sanitize(id));

        NotificationDTO notification =
                notificationService.getById(id);

        return ResponseEntity.ok(notification);
    }

    @GetMapping(value = "/user/{email}", produces =   MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(
            @PathVariable("email") String email
    ) {

        log.info("Fetching notifications for user email: {}", LogSanitizer.sanitize(email));

        List<NotificationDTO> notifications =
                notificationService.getByUserEmail(email);

        log.info(
                "Found {} notifications for user email: {}",
                notifications.size(),
                LogSanitizer.sanitize(email)
        );

        return ResponseEntity.ok(notifications);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PutMapping(value = "/{id}/read", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") Long id
    ) {

        log.info("Marking notification as read with id: {}", id);

        notificationService.markAsRead(id);

        log.info(
                "Notification marked as read successfully with id: {}",
                id
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/user/{userId}/read", produces =   MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> markAllAsReadForUser(
            @PathVariable("userId") Long userId
    ) {

        log.info(
                "Marking all notifications as read for user id: {}",
                userId
        );

        notificationService.markAllAsReadForUser(userId);

        log.info(
                "All notifications marked as read successfully for user id: {}",
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{id}", produces =   MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteNotification(
            @PathVariable("id") Long id
    ) {

        log.warn("Deleting notification with id: {}", id);

        notificationService.deleteById(id);

        log.info(
                "Notification deleted successfully with id: {}",
                id
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/user/{userId}", produces =    MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAllNotificationsForUser(
            @PathVariable("userId") Long userId
    ) {

        log.warn(
                "Deleting all notifications for user id: {}",
                userId
        );

        notificationService.deleteAllForUser(userId);

        log.info(
                "All notifications deleted successfully for user id: {}",
                userId
        );

        return ResponseEntity.noContent().build();
    }
}