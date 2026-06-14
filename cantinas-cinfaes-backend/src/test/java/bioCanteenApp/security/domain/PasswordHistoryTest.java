package bioCanteenApp.security.domain;

import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHistoryTest {

    @Test
    void shouldCreateEmptyPasswordHistory() {
        PasswordHistory passwordHistory = new PasswordHistory();

        assertNull(passwordHistory.getId());
        assertNull(passwordHistory.getUser());
        assertNull(passwordHistory.getPassword());
        assertNull(passwordHistory.getCreatedAt());
    }

    @Test
    void shouldCreatePasswordHistoryWithConstructor() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        PasswordHistory passwordHistory =
                new PasswordHistory(
                        user,
                        "encodedPassword"
                );

        assertEquals(user, passwordHistory.getUser());
        assertEquals("encodedPassword", passwordHistory.getPassword());
    }

    @Test
    void shouldSetAndGetId() {
        PasswordHistory passwordHistory =
                new PasswordHistory();

        passwordHistory.setId(1L);

        assertEquals(1L, passwordHistory.getId());
    }

    @Test
    void shouldSetAndGetUser() {
        PasswordHistory passwordHistory =
                new PasswordHistory();

        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        passwordHistory.setUser(user);

        assertEquals(user, passwordHistory.getUser());
    }

    @Test
    void shouldSetAndGetPassword() {
        PasswordHistory passwordHistory =
                new PasswordHistory();

        passwordHistory.setPassword("newPassword");

        assertEquals(
                "newPassword",
                passwordHistory.getPassword()
        );
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        PasswordHistory passwordHistory =
                new PasswordHistory();

        LocalDateTime createdAt =
                LocalDateTime.now();

        passwordHistory.setCreatedAt(createdAt);

        assertEquals(
                createdAt,
                passwordHistory.getCreatedAt()
        );
    }
}