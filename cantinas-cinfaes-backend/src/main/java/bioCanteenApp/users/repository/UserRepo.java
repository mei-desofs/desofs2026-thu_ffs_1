package bioCanteenApp.users.repository;

import bioCanteenApp.users.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public Iterable<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", email);

        return query.getResultList();
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
        List<User> users = (List<User>) findByEmail(email);

        for (User u : users) {
            entityManager.remove(entityManager.contains(u) ? u : entityManager.merge(u));
        }
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
}
