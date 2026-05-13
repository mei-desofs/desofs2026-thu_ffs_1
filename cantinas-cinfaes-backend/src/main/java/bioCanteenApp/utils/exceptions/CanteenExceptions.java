package bioCanteenApp.utils.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CanteenExceptions {

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class CanteenAlreadyExists extends RuntimeException {
        public CanteenAlreadyExists(String name) {
            super("Canteen with name '" + name + "' already exists.");
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class CanteenNotFound extends RuntimeException {
        public CanteenNotFound(Long id) {
            super("No canteen found with id = " + id);
        }
    }
}
