package bioCanteenApp.utils.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class NotificationExceptions {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotificationNotFound extends RuntimeException {
        public NotificationNotFound(Long id) {
            super("No notification found with id = " + id);
        }
    }
}
