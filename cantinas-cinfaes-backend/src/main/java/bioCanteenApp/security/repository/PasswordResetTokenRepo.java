package bioCanteenApp.security.repository;

import bioCanteenApp.security.domain.PasswordHistory;
import bioCanteenApp.security.domain.PasswordResetToken;
import bioCanteenApp.users.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PasswordResetTokenRepo implements IPasswordResetTokenRepo {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public void save(PasswordResetToken token) {
        if (token.getId() == null) {
            entityManager.persist(token);
        } else {
            entityManager.merge(token);
        }
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        List<PasswordResetToken> result = entityManager.createQuery(
                        "SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token", PasswordResetToken.class)
                .setParameter("token", token)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        entityManager.createQuery("DELETE FROM PasswordResetToken prt WHERE prt.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
