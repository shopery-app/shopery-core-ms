package az.shopery.kafka.producer;

import az.shopery.model.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${topic.notification}")
    private String notificationTopic;

    public void send(NotificationEvent event) {
        kafkaTemplate.send(notificationTopic, event);
    }
}
