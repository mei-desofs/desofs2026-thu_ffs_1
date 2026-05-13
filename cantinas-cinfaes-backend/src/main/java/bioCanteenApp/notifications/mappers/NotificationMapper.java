package bioCanteenApp.notifications.mappers;

import bioCanteenApp.notifications.domain.Notification;
import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.IUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationMapper implements INotificationMapper {

    private final IUserRepo userRepo;

    @Override
    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) return null;

        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .actionUrl(notification.getActionUrl())
                .priority(notification.getPriority())
                .build();
    }

    @Override
    public Notification toDomain(NotificationDTO dto) {
        if (dto == null) return null;

        return new Notification(
                dto.getUserId() != null ? userRepo.findById(dto.getUserId()) : null,
                dto.getTitle(),
                dto.getMessage(),
                dto.getType(),
                dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now(),
                dto.isRead(),
                dto.getActionUrl(),
                dto.getPriority()
        );
    }
}
