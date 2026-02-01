package az.shopery.listener;

import az.shopery.handler.notification.NotificationHandler;
import az.shopery.model.event.NotificationEvent;
import az.shopery.utils.enums.NotificationType;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationEventListener {

    private final Map<NotificationType, NotificationHandler<?>> handlers;

    public NotificationEventListener(
            List<NotificationHandler<?>> handlerList) {

        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        NotificationHandler::supports,
                        Function.identity()
                ));
    }

    @EventListener
    public <T> void onNotification(NotificationEvent<T> event) {

        @SuppressWarnings("unchecked")
        NotificationHandler<T> handler =
                (NotificationHandler<T>) handlers.get(event.type());

        if (handler == null) {
            throw new IllegalStateException(
                    "No handler found for " + event.type()
            );
        }

        handler.handle(event.payload());
    }
}
