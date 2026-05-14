package bioCanteenApp.security.repository;

import bioCanteenApp.security.domain.PasswordHistory;
import bioCanteenApp.users.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PasswordHistoryRepo implements IPasswordHistoryRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PasswordHistory> findTop5ByUserOrderByCreatedAtDesc(User user) {
        return entityManager.createQuery(
                        "SELECT ph FROM PasswordHistory ph WHERE ph.user = :user ORDER BY ph.createdAt DESC",
                        PasswordHistory.class)
                .setParameter("user", user)
                .setMaxResults(5)
                .getResultList();
    }

    @Override
    public void save(PasswordHistory passwordHistory) {
        if (passwordHistory.getId() == null) {
            entityManager.persist(passwordHistory);
        } else {
            entityManager.merge(passwordHistory);
        }
    }
}
