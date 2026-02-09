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
import az.shopery.utils.aws.S3FileUtil;
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
import java.util.HashMap;
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
    private final S3FileUtil s3FileUtil;

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
    public SuccessResponse<Map<String, Integer>> getApplicationInfo(String userEmail) {
        UserEntity assignedAdmin = getAdmin(userEmail);
        Map<String, Integer> applicationInfo = new HashMap<>();

        Integer totalCustomers = userRepository.countAllByUserRoleAndStatus(UserRole.CUSTOMER, UserStatus.ACTIVE);
        Integer totalMerchants = userRepository.countAllByUserRoleAndStatus(UserRole.MERCHANT, UserStatus.ACTIVE);
        Integer pendingSupportTickets = taskRepository.countSupportTicketsByStatusAndAdmin(TicketStatus.OPEN, assignedAdmin.getId());
        Integer pendingShopCreationRequests = taskRepository.countShopRequestsByStatusAndAdmin(RequestStatus.PENDING, assignedAdmin.getId());

        applicationInfo.put("totalCustomers", totalCustomers);
        applicationInfo.put("totalMerchants", totalMerchants);
        applicationInfo.put("pendingSupportTickets", pendingSupportTickets);
        applicationInfo.put("pendingShopCreationRequests", pendingShopCreationRequests);
        applicationInfo.put("totalTasks", pendingShopCreationRequests + pendingSupportTickets);

        return SuccessResponse.of(applicationInfo, "Application info retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> closeSupportTicket(String id, String userEmail) {
        TaskEntity taskEntity = taskRepository.findByIdAndAssignedAdmin(parse(id), getAdmin(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found!"));

        if (!(taskEntity instanceof SupportTicketEntity supportTicketEntity)) {
            throw new IllegalRequestException("Task is not a support ticket!");
        }
        if (supportTicketEntity.getTicketStatus().equals(TicketStatus.CLOSED)) {
            throw new  IllegalRequestException("Ticket already closed!");
        }

        supportTicketEntity.setTicketStatus(TicketStatus.CLOSED);
        return SuccessResponse.of("Support ticket has been closed successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> approve(String id, String userEmail) {
        ShopCreationRequestEntity shopCreationRequestEntity = getShopCreationRequestEntity(id, userEmail);
        if (!shopCreationRequestEntity.getRequestStatus().equals(RequestStatus.PENDING)) {
            throw new IllegalRequestException("Shop creation request is not pending!");
        }

        ShopEntity shop = ShopEntity.builder()
                .user(shopCreationRequestEntity.getCreatedBy())
                .shopName(shopCreationRequestEntity.getShopName())
                .description(shopCreationRequestEntity.getDescription())
                .totalIncome(BigDecimal.ZERO)
                .rating(0.0)
                .build();
        shopRepository.save(shop);
        shopCreationRequestEntity.setRequestStatus(RequestStatus.APPROVED);

        UserEntity userEntity = userRepository.findByEmailAndUserRoleAndStatus(shopCreationRequestEntity.getCreatedBy().getEmail(), UserRole.MERCHANT, UserStatus.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException("Merchant not found!"));
        userEntity.setSubscriptionTier(shopCreationRequestEntity.getSubscriptionTier());
        applicationEventPublisher.publishEvent(new NotificationEvent(
                userEmail,
                NotificationType.SHOP_APPROVED,
                Map.of(
                        "userName",shopCreationRequestEntity.getCreatedBy().getName(),
                        "shopName",shopCreationRequestEntity.getShopName()
                )
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
                Map.of(
                        "userName",shopCreationRequestEntity.getCreatedBy().getName(),
                        "shopName",shopCreationRequestEntity.getShopName()
                )
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
                .profilePhotoUrl(s3FileUtil.generatePresignedUrl(userEntity.getProfilePhotoUrl()))
                .createdAt(userEntity.getCreatedAt())
                .build();
    }
}
