package az.shopery.handler.notification;

import az.shopery.utils.enums.NotificationType;

public interface NotificationHandler<T> {

    NotificationType supports();

    void handle(T event);
}
