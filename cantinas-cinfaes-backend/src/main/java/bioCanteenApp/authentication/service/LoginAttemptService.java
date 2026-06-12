package bioCanteenApp.authentication.service;

import bioCanteenApp.authentication.exception.AccountLockedException;
import bioCanteenApp.email.service.EmailService;
import bioCanteenApp.users.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS   = 3;
    private static final int LOCK_MINUTES   = 5;

    private final UserRepo userRepository;
    private final EmailService emailService;

    public void checkNotLocked(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isLocked()) {
                log.warn("Login bloqueado para: {}", email);
                throw new AccountLockedException(user.secondsUntilUnlock());
            }
        });
    }


    // Chamado quando a password está errada.
    public void registerFailure(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {

            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
                user.setLockedUntil(lockUntil);

                log.warn("Conta bloqueada por {} minutos: {}", LOCK_MINUTES, email);
                emailService.sendLockNotification(email, LOCK_MINUTES); // REQ1.3
            }

            userRepository.save(user);
        });
    }


    // Chamado quando o login é bem-sucedido. Limpa o contador e o bloqueio.
    public void resetAttempts(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {

            if (user.getFailedAttempts() > 0 || user.getLockedUntil() != null) {
                user.setFailedAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }
        });
    }

}
