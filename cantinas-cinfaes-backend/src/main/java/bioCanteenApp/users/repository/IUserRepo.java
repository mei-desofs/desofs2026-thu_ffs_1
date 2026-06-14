package bioCanteenApp.users.repository;

import bioCanteenApp.users.domain.User;

import java.util.Optional;

public interface IUserRepo {
    Iterable<User> findAll();
    Optional<User> findByEmail(String email);
    User save(User user);
    void delete(String email);
    User findById(Long id);
    User findCentralCanteenManager();
    Optional<User> findByRefreshToken(String refreshToken);
}
