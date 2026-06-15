package bioCanteenApp.users.repository;

import bioCanteenApp.users.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class UserRepo implements IUserRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Iterable<User> findAll() {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u", User.class);

        return query.getResultList();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class
        );

        query.setParameter("email", email);

        List<User> results = query.getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(results.get(0));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            user = entityManager.merge(user);
        }

        return user;
    }

    @Override
    public void delete(String email) {
        Optional<User> userOpt = findByEmail(email);
        userOpt.ifPresent(entityManager::remove);
    }

    @Override
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public User findCentralCanteenManager() {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.role = 'CANTEEN_MANAGER'",
                User.class
        );
        List<User> result = query.getResultList();
        return result.isEmpty() ? null : result.getFirst();
    }

    @Override
    public Optional<User> findByRefreshToken(String refreshToken) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.refreshToken = :refreshToken",
                User.class
        );

        query.setParameter("refreshToken", refreshToken);

        List<User> results = query.getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
