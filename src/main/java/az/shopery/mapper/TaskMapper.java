package az.shopery.mapper;

import az.shopery.handler.exception.ApplicationException;
import az.shopery.model.dto.response.task.ShopCreationRequestResponseDto;
import az.shopery.model.dto.response.task.SupportTicketResponseDto;
import az.shopery.model.dto.response.task.TaskResponseDto;
import az.shopery.model.dto.shared.TaskCreatorDto;
import az.shopery.model.entity.task.ShopCreationRequestEntity;
import az.shopery.model.entity.task.SupportTicketEntity;
import az.shopery.model.entity.task.TaskEntity;
import az.shopery.utils.enums.TaskCategory;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponseDto toDto(TaskEntity taskEntity) {

        TaskCreatorDto taskCreatorDto = TaskCreatorDto.builder()
                .id(taskEntity.getCreatedBy().getId())
                .name(taskEntity.getCreatedBy().getName())
                .email(taskEntity.getCreatedBy().getEmail())
                .phone(taskEntity.getCreatedBy().getPhone())
                .build();

        if (taskEntity instanceof SupportTicketEntity supportTicketEntity) {
            return SupportTicketResponseDto.builder()
                    .id(supportTicketEntity.getId())
                    .taskCategory(TaskCategory.SUPPORT_TICKET)
                    .taskCreatorDto(taskCreatorDto)
                    .createdAt(supportTicketEntity.getCreatedAt())
                    .updatedAt(supportTicketEntity.getUpdatedAt())
                    .supportTicketSubject(supportTicketEntity.getSubject())
                    .supportTicketDescription(supportTicketEntity.getDescription())
                    .ticketStatus(supportTicketEntity.getTicketStatus())
                    .build();
        }

        if (taskEntity instanceof ShopCreationRequestEntity shopCreationRequestEntity) {
            return ShopCreationRequestResponseDto.builder()
                    .id(shopCreationRequestEntity.getId())
                    .taskCategory(TaskCategory.SHOP_CREATION_REQUEST)
                    .taskCreatorDto(taskCreatorDto)
                    .createdAt(shopCreationRequestEntity.getCreatedAt())
                    .updatedAt(shopCreationRequestEntity.getUpdatedAt())
                    .shopName(shopCreationRequestEntity.getShopName())
                    .shopDescription(shopCreationRequestEntity.getDescription())
                    .subscriptionTier(shopCreationRequestEntity.getSubscriptionTier())
                    .requestStatus(shopCreationRequestEntity.getRequestStatus())
                    .rejectionReason(shopCreationRequestEntity.getRejectionReason())
                    .build();
        }

        throw new ApplicationException("Unsupported task type: " + taskEntity.getClass().getSimpleName());
    }
}
