package bioCanteenApp.notifications.repository;

import bioCanteenApp.notifications.domain.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class NotificationRepo implements INotificationRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            entityManager.persist(notification);
        } else {
            notification = entityManager.merge(notification);
        }
        return notification;
    }

    @Override
    public Optional<Notification> findById(Long id) {
        Notification notification = entityManager.find(Notification.class, id);
        return Optional.ofNullable(notification);
    }

    @Override
    public List<Notification> findAll() {
        TypedQuery<Notification> query =
                entityManager.createQuery("SELECT n FROM Notification n ORDER BY n.createdAt DESC",
                        Notification.class);
        return query.getResultList();
    }

    @Override
    public List<Notification> findByUserEmail(String userId) {
        TypedQuery<Notification> query =
                entityManager.createQuery(
                        "SELECT n FROM Notification n WHERE n.user.email = :userId ORDER BY n.createdAt DESC",
                        Notification.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    public List<Notification> findUnreadByUserId(Long userId) {
        TypedQuery<Notification> query =
                entityManager.createQuery(
                        "SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false ORDER BY n.createdAt DESC",
                        Notification.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = entityManager.find(Notification.class, id);
        if (notification != null) {
            notification.setRead(true);
            entityManager.merge(notification);
        }
    }

    @Override
    public void markAllAsReadForUser(Long userId) {
        entityManager.createQuery(
                        "UPDATE Notification n SET n.read = true WHERE n.user.id = :userId"
                ).setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void deleteById(Long id) {
        Notification notification = entityManager.find(Notification.class, id);
        if (notification != null) {
            entityManager.remove(notification);
        }
    }

    @Override
    public void deleteAllForUser(Long userId) {
        entityManager.createQuery(
                        "DELETE FROM Notification n WHERE n.user.id = :userId"
                ).setParameter("userId", userId)
                .executeUpdate();
    }
}
