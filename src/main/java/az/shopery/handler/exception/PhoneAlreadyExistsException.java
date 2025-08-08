package az.shopery.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException(String message) {
        super(message);
    }
}
