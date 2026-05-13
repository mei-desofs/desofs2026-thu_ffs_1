package bioCanteenApp.notifications.service;

import bioCanteenApp.notifications.domain.Notification;
import bioCanteenApp.notifications.dto.NotificationDTO;
import bioCanteenApp.notifications.mappers.INotificationMapper;
import bioCanteenApp.notifications.repository.INotificationRepo;
import org.springframework.stereotype.Service;
import java.util.List;

import static bioCanteenApp.utils.exceptions.NotificationExceptions.*;

@Service
public class NotificationService implements INotificationService {

    private final INotificationRepo repo;
    private final INotificationMapper mapper;

    public NotificationService(INotificationRepo repo, INotificationMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }


    public NotificationDTO createNotification(NotificationDTO dto) {
        Notification notification = mapper.toDomain(dto);

        Notification saved = repo.save(notification);
        return mapper.toDTO(saved);
    }

    public List<NotificationDTO> getAllNotifications() {
        return repo.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public NotificationDTO getById(Long id) {
        Notification notification = repo.findById(id)
                .orElseThrow(() -> new NotificationNotFound(id));

        return mapper.toDTO(notification);
    }

    public List<NotificationDTO> getByUserEmail(String userId) {
        return repo.findByUserEmail(userId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public void markAsRead(Long id) {
        Notification notification = repo.findById(id)
                .orElseThrow(() -> new NotificationNotFound(id));

        repo.markAsRead(id);
    }

    public void markAllAsReadForUser(Long userId) {
        repo.markAllAsReadForUser(userId);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public void deleteAllForUser(Long userId) {
        repo.deleteAllForUser(userId);
    }
}

