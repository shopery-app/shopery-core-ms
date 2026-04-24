package az.shopery.service.impl;

import az.shopery.handler.exception.ApplicationException;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.task.ShopCreationRequestEntity;
import az.shopery.model.entity.task.SupportTicketEntity;
import az.shopery.model.event.TaskEvent;
import az.shopery.repository.TaskRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.TaskService;
import az.shopery.utils.enums.SubscriptionTier;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createTask(TaskEvent event) {
        UserEntity assignedAdmin = userRepository.findRandomActiveAdmin()
                .orElseThrow(() -> new ApplicationException("No admin available!"));

        switch (event.category()) {
            case SUPPORT_TICKET -> createSupportTicket(event, assignedAdmin);
            case SHOP_CREATION_REQUEST -> createShopCreationRequest(event, assignedAdmin);
            default -> throw new ApplicationException("Unsupported task type!");
        }
    }

    private void createSupportTicket(TaskEvent event, UserEntity assignedAdmin) {
        SupportTicketEntity ticket = SupportTicketEntity.builder()
                .createdBy(event.createdBy())
                .assignedAdmin(assignedAdmin)
                .subject(get(event, "subject", String.class))
                .description(get(event, "description", String.class))
                .build();

        taskRepository.save(ticket);
    }

    private void createShopCreationRequest(TaskEvent event, UserEntity assignedAdmin) {
        ShopCreationRequestEntity request = ShopCreationRequestEntity.builder()
                .createdBy(event.createdBy())
                .assignedAdmin(assignedAdmin)
                .shopName(get(event, "shopName", String.class))
                .description(get(event, "description", String.class))
                .subscriptionTier(get(event, "subscriptionTier", SubscriptionTier.class))
                .build();

        taskRepository.save(request);
    }

    private <T> T get(TaskEvent event, String key, Class<T> type) {
        Object value = event.params().get(key);

        if (Objects.isNull(value)) {
            throw new ApplicationException("Missing task parameter: " + key);
        }

        if (!type.isInstance(value)) {
            throw new ApplicationException("Invalid task parameter type: " + key);
        }

        return type.cast(value);
    }
}
