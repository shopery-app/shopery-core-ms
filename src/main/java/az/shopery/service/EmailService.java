package az.shopery.service;

import az.shopery.model.event.NotificationEvent;

public interface EmailService {
    void sendNotification(NotificationEvent notificationEvent);
}
