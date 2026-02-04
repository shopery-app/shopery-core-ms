package az.shopery.service.impl;

import static az.shopery.utils.common.NameMapperHelper.first;
import static az.shopery.utils.common.NameMapperHelper.last;
import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.mapper.TaskMapper;
import az.shopery.model.dto.request.CloseMerchantRequestDto;
import az.shopery.model.dto.request.ShopCreationRequestRejectDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.dto.response.task.TaskResponseDto;
import az.shopery.model.entity.OrderEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.model.entity.task.ShopCreationRequestEntity;
import az.shopery.model.entity.task.SupportTicketEntity;
import az.shopery.model.entity.task.TaskEntity;
import az.shopery.model.event.NotificationEvent;
import az.shopery.repository.OrderRepository;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.TaskRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.AdminService;
import az.shopery.utils.enums.NotificationType;
import az.shopery.utils.enums.OrderStatus;
import az.shopery.utils.enums.RequestStatus;
import az.shopery.utils.enums.TaskCategory;
import az.shopery.utils.enums.TicketStatus;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TaskRepository taskRepository;
    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TaskMapper taskMapper;

    @Override
    public SuccessResponse<Page<UserProfileResponseDto>> getCustomers(Pageable pageable) {
        Page<UserEntity> customers = userRepository.findAllByUserRoleAndStatus(UserRole.CUSTOMER, UserStatus.ACTIVE, pageable);
        return SuccessResponse.of(customers.map(this::mapToDto), "Customers are retrieved successfully!");
    }

    @Override
    public SuccessResponse<Page<UserProfileResponseDto>> getMerchants(Pageable pageable) {
        Page<UserEntity> customers = userRepository.findAllByUserRoleAndStatus(UserRole.MERCHANT, UserStatus.ACTIVE, pageable);
        return SuccessResponse.of(customers.map(this::mapToDto), "Merchants are retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> closeMerchant(CloseMerchantRequestDto closeMerchantRequestDto) {
        String email = closeMerchantRequestDto.getEmail();
        UserEntity user = userRepository.findByEmailAndUserRoleAndStatus(email, UserRole.MERCHANT, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found!"));
        user.setStatus(UserStatus.CLOSED);

        ShopEntity shop = user.getShop();
        List<OrderEntity> orders = orderRepository.findAllByShopId(shop.getId());
        for (OrderEntity order : orders) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        return SuccessResponse.of("User deleted successfully!");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<Page<TaskResponseDto>> getTasks(TaskCategory taskCategory, Pageable pageable, String userEmail) {
        UserEntity assignedAdmin = getAdmin(userEmail);
        Page<TaskEntity> tasks = (Objects.isNull(taskCategory))
                ? taskRepository.findAllByAssignedAdmin(assignedAdmin, pageable)
                : taskRepository.findAllByAssignedAdminAndTaskCategory(assignedAdmin, taskCategory, pageable);

        return SuccessResponse.of(tasks.map(taskMapper::toDto), "Tasks are retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> closeSupportTicket(String id, String userEmail) {
        TaskEntity taskEntity = taskRepository.findByIdAndAssignedAdmin(parse(id), getAdmin(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));

        if (!(taskEntity instanceof SupportTicketEntity supportTicketEntity)) {
            throw new IllegalRequestException("Task is not a support ticket!");
        }
        supportTicketEntity.setTicketStatus(TicketStatus.CLOSED);

        return SuccessResponse.of("Support ticket has been closed successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> approve(String id, String userEmail) {
        ShopCreationRequestEntity shopCreationRequestEntity = getShopCreationRequestEntity(id, userEmail);

        ShopEntity shop = ShopEntity.builder()
                .user(shopCreationRequestEntity.getCreatedBy())
                .shopName(shopCreationRequestEntity.getShopName())
                .description(shopCreationRequestEntity.getDescription())
                .totalIncome(BigDecimal.ZERO)
                .rating(0.0)
                .build();
        shopRepository.save(shop);
        shopCreationRequestEntity.setRequestStatus(RequestStatus.APPROVED);

        applicationEventPublisher.publishEvent(new NotificationEvent(
                userEmail,
                NotificationType.SHOP_APPROVED,
                Map.of()
        ));
        return SuccessResponse.of("Shop creation request has been approved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> reject(String id, String userEmail, ShopCreationRequestRejectDto shopCreationRequestRejectDto) {
        ShopCreationRequestEntity shopCreationRequestEntity = getShopCreationRequestEntity(id, userEmail);

        shopCreationRequestEntity.setRequestStatus(RequestStatus.REJECTED);
        shopCreationRequestEntity.setRejectionReason(shopCreationRequestRejectDto.getReason());

        applicationEventPublisher.publishEvent(new NotificationEvent(
                userEmail,
                NotificationType.SHOP_REJECTED,
                Map.of()
        ));
        return SuccessResponse.of("Shop creation request has been rejected successfully!");
    }

    private UserEntity getAdmin(String userEmail) {
        return userRepository.findByEmailAndUserRoleAndStatus(userEmail, UserRole.ADMIN, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found!"));
    }

    private ShopCreationRequestEntity getShopCreationRequestEntity(String id, String userEmail) {
        TaskEntity taskEntity = taskRepository.findByIdAndAssignedAdmin(parse(id), getAdmin(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));

        if (!(taskEntity instanceof ShopCreationRequestEntity shopCreationRequestEntity)) {
            throw new IllegalRequestException("Task is not a shop creation request!");
        }

        return shopCreationRequestEntity;
    }

    private UserProfileResponseDto mapToDto(UserEntity userEntity) {
        return UserProfileResponseDto.builder()
                .firstName(first(userEntity.getName()))
                .lastName(last(userEntity.getName()))
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .dateOfBirth(userEntity.getDateOfBirth())
                .profilePhotoUrl(userEntity.getProfilePhotoUrl())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }
}
