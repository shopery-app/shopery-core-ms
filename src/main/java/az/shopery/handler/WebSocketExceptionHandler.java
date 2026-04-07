package az.shopery.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(Exception.class)
    public void handle(Exception ex) {
        log.error("WebSocket message handling failed", ex);
        try {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
