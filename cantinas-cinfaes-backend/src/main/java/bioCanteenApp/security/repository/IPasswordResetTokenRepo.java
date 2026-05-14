package bioCanteenApp.security.repository;

import bioCanteenApp.security.domain.PasswordResetToken;

import java.util.Optional;

public interface IPasswordResetTokenRepo {
    void save(PasswordResetToken token);
    Optional<PasswordResetToken> findByToken(String token);
    void deleteAllByUserId(Long userId);
}
