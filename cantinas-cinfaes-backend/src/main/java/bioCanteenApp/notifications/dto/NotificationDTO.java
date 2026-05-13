package bioCanteenApp.notifications.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private String actionUrl;
    private Integer priority;
    private boolean isRead;
    private LocalDateTime createdAt;
}

