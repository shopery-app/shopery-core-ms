package az.shopery.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class CooldownNotMetException extends RuntimeException {
    public CooldownNotMetException(String message) {
        super(message);
    }
}
