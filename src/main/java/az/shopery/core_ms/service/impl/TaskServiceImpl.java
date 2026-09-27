package az.shopery.core_ms.service.impl;

import az.shopery.core_ms.handler.exception.ApplicationException;
import az.shopery.core_ms.model.entity.UserEntity;
import az.shopery.core_ms.model.entity.task.ShopCreationRequestEntity;
import az.shopery.core_ms.model.entity.task.SupportTicketEntity;
import az.shopery.core_ms.model.event.TaskEvent;
import az.shopery.core_ms.repository.TaskRepository;
import az.shopery.core_ms.repository.UserRepository;
import az.shopery.core_ms.service.TaskService;
import az.shopery.core_ms.utils.enums.SubscriptionTier;
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
