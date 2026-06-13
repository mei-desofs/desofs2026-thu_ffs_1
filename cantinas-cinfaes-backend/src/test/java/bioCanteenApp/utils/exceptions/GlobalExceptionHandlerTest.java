package bioCanteenApp.utils.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleCanteenAlreadyExists_ShouldReturn409() {
        CanteenExceptions.CanteenAlreadyExists ex =
                new CanteenExceptions.CanteenAlreadyExists("Cantina Central");

        ResponseEntity<?> response = handler.handleCanteenAlreadyExists(ex);

        assertEquals(409, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertEquals("Canteen already exists", body.get("error"));
        assertTrue(body.get("message").toString().contains("Cantina Central"));
    }

    @Test
    void handleCanteenNotFound_ShouldReturn404() {
        CanteenExceptions.CanteenNotFound ex =
                new CanteenExceptions.CanteenNotFound(42L);

        ResponseEntity<?> response = handler.handleCanteenNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Canteen not found", body.get("error"));
        assertTrue(body.get("message").toString().contains("42"));
    }

    @Test
    void handleReservationNotFound_ShouldReturn404() {
        ReservationExceptions.ReservationNotFound ex =
                new ReservationExceptions.ReservationNotFound(7L);

        ResponseEntity<?> response = handler.handleReservationNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Reservation not found", body.get("error"));
        assertTrue(body.get("message").toString().contains("7"));
    }

    @Test
    void handleReservationAlreadyExists_ShouldReturn409() {
        ReservationExceptions.ReservationAlreadyExists ex =
                new ReservationExceptions.ReservationAlreadyExists(3L);

        ResponseEntity<?> response = handler.handleReservationAlreadyExists(ex);

        assertEquals(409, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertEquals("Reservation already exists", body.get("error"));
        assertTrue(body.get("message").toString().contains("3"));
    }

    @Test
    void handleNotificationNotFound_ShouldReturn404() {
        NotificationExceptions.NotificationNotFound ex =
                new NotificationExceptions.NotificationNotFound(99L);

        ResponseEntity<?> response = handler.handleNotificationNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Notification not found", body.get("error"));
        assertTrue(body.get("message").toString().contains("99"));
    }
}
