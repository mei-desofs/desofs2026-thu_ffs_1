package bioCanteenApp.security.repository;

import bioCanteenApp.security.domain.PasswordHistory;
import bioCanteenApp.users.domain.User;

import java.util.List;

public interface IPasswordHistoryRepo {
    List<PasswordHistory> findTop5ByUserOrderByCreatedAtDesc(User user);
    void save(PasswordHistory passwordHistory);
}
