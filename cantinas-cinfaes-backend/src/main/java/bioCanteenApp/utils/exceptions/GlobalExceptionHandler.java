package bioCanteenApp.utils.exceptions;

import bioCanteenApp.authentication.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import bioCanteenApp.authentication.exception.AccountLockedException;
import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CanteenExceptions.CanteenAlreadyExists.class)
    public ResponseEntity<?> handleCanteenAlreadyExists(CanteenExceptions.CanteenAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.CONFLICT.value(),
                        "error", "Canteen already exists",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(CanteenExceptions.CanteenNotFound.class)
    public ResponseEntity<?> handleCanteenNotFound(CanteenExceptions.CanteenNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.NOT_FOUND.value(),
                        "error", "Canteen not found",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(ReservationExceptions.ReservationNotFound.class)
    public ResponseEntity<?> handleReservationNotFound(ReservationExceptions.ReservationNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.NOT_FOUND.value(),
                        "error", "Reservation not found",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(ReservationExceptions.ReservationAlreadyExists.class)
    public ResponseEntity<?> handleReservationAlreadyExists(ReservationExceptions.ReservationAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.CONFLICT.value(),
                        "error", "Reservation already exists",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(NotificationExceptions.NotificationNotFound.class)
    public ResponseEntity<?> handleNotificationNotFound(NotificationExceptions.NotificationNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.NOT_FOUND.value(),
                        "error", "Notification not found",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(401).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 401,
                        "error", "Invalid credentials",
                        "message", "Invalid email or password."
                )
        );
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<?> handleLocked(AccountLockedException ex) {
        return ResponseEntity.status(429).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 429,
                        "error", "Account locked",
                        "message", "Try again in " + ex.getSecondsRemaining() + " seconds."
                )
        );
    }

}
