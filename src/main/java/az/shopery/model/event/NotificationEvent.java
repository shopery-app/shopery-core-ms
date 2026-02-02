package az.shopery.model.event;

import az.shopery.utils.enums.NotificationType;

public record NotificationEvent<T>(
        NotificationType type,
        T function
) {}
