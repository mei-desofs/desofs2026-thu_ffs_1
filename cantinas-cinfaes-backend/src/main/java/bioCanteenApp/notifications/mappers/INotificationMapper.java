package bioCanteenApp.notifications.mappers;

import bioCanteenApp.notifications.domain.Notification;
import bioCanteenApp.notifications.dto.NotificationDTO;

public interface INotificationMapper {
    NotificationDTO toDTO(Notification canteen);
    Notification toDomain(NotificationDTO dto);
}
