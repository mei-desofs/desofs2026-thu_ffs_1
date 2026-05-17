package bioCanteenApp.security.domain;

import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    @Test
    void shouldCreateEmptyPasswordResetToken() {
        PasswordResetToken token =
                new PasswordResetToken();

        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getUser());
        assertNull(token.getExpiresAt());
        assertFalse(token.isUsed());
    }

    @Test
    void shouldCreatePasswordResetTokenWithConstructor() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        PasswordResetToken token =
                new PasswordResetToken(
                        user,
                        "token123"
                );

        assertEquals("token123", token.getToken());
        assertEquals(user, token.getUser());

        assertNotNull(token.getExpiresAt());
        assertFalse(token.isUsed());

        assertTrue(
                token.getExpiresAt()
                        .isAfter(LocalDateTime.now())
        );
    }

    @Test
    void shouldSetAndGetId() {
        PasswordResetToken token =
                new PasswordResetToken();

        token.setId(1L);

        assertEquals(1L, token.getId());
    }

    @Test
    void shouldSetAndGetToken() {
        PasswordResetToken token =
                new PasswordResetToken();

        token.setToken("abc123");

        assertEquals(
                "abc123",
                token.getToken()
        );
    }

    @Test
    void shouldSetAndGetUser() {
        PasswordResetToken token =
                new PasswordResetToken();

        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        token.setUser(user);

        assertEquals(user, token.getUser());
    }

    @Test
    void shouldSetAndGetExpiresAt() {
        PasswordResetToken token =
                new PasswordResetToken();

        LocalDateTime expiresAt =
                LocalDateTime.now().plusMinutes(20);

        token.setExpiresAt(expiresAt);

        assertEquals(
                expiresAt,
                token.getExpiresAt()
        );
    }

    @Test
    void shouldSetAndGetUsed() {
        PasswordResetToken token =
                new PasswordResetToken();

        token.setUsed(true);

        assertTrue(token.isUsed());
    }

    @Test
    void shouldReturnFalseWhenTokenIsNotExpired() {
        User user = new User(
                "user@email.com",
                "User",
                "password",
                Role.USER
        );

        PasswordResetToken token =
                new PasswordResetToken(
                        user,
                        "token123"
                );

        assertFalse(token.isExpired());
    }

    @Test
    void shouldReturnTrueWhenTokenIsExpired() {
        PasswordResetToken token =
                new PasswordResetToken();

        token.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        assertTrue(token.isExpired());
    }
}