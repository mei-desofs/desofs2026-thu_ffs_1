package bioCanteenApp.utils.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ReservationExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ReservationNotFound extends RuntimeException {
        public ReservationNotFound(Long id) {
            super("No reservation found with id = " + id);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ReservationAlreadyExists extends RuntimeException {
        public ReservationAlreadyExists(Long userId) {
            super("User with id = " + userId + " already has a reservation");
        }

        public ReservationAlreadyExists(String message) {
            super(message);
        }
    }
}
