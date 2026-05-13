package bioCanteenApp.users.repository;

import bioCanteenApp.users.domain.User;

public interface IUserRepo {
    Iterable<User> findAll();
    Iterable<User> findByEmail(String email);
    User save(User user);
    void delete(String email);
    User findById(Long id);
    User findCentralCanteenManager();
}
