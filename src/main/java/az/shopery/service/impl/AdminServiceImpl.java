package az.shopery.service.impl;

import static az.shopery.utils.common.NameMapperHelper.first;
import static az.shopery.utils.common.NameMapperHelper.last;
import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.mapper.TaskMapper;
import az.shopery.model.dto.projection.AdminShopProjection;
import az.shopery.model.dto.request.ShopCreationRequestRejectDto;
import az.shopery.model.dto.response.AdminShopResponseDto;
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
import az.shopery.utils.enums.ShopStatus;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TaskMapper taskMapper;
    private final S3FileUtil s3FileUtil;

    @Override
    public SuccessResponse<Page<UserProfileResponseDto>> getUsers(Pageable pageable) {
        Page<UserEntity> users = userRepository.findAllByUserRoleAndStatus(UserRole.USER, UserStatus.ACTIVE, pageable);
        return SuccessResponse.of(users.map(this::mapToDto), "Users are retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> closeUser(String id) {
        UserEntity user = userRepository.findById(parse(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        user.setStatus(UserStatus.CLOSED);

        List<ShopEntity> shops = user.getShops();
        for (ShopEntity shop : shops) {
            shop.setStatus(ShopStatus.CLOSED);
            List<OrderEntity> orders = orderRepository.findAllByShopId(shop.getId());
            for (OrderEntity order : orders) {
                order.setStatus(OrderStatus.CANCELLED);
            }
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
        shopCreationRequestEntity.setRequestStatus(RequestStatus.APPROVED);

        UserEntity userEntity = userRepository.findByEmailAndUserRoleAndStatus(shopCreationRequestEntity.getCreatedBy().getEmail(), UserRole.USER, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        userEntity.setSubscriptionTier(shopCreationRequestEntity.getSubscriptionTier());
        ShopEntity shopEntity = shopRepository.findByShopName(shopCreationRequestEntity.getShopName())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found!"));
        shopEntity.setStatus(ShopStatus.ACTIVE);
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

        ShopEntity shopEntity = shopRepository.findByShopName(shopCreationRequestEntity.getShopName())
                        .orElseThrow(() -> new ResourceNotFoundException("Shop not found!"));
        shopEntity.setStatus(ShopStatus.CLOSED);

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

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<Page<AdminShopResponseDto>> getShops(Pageable pageable) {
        Page<AdminShopProjection> shops = shopRepository.findAllWithProductCount(pageable);
        return SuccessResponse.of(shops.map(this::mapToAdminShopDto), "All shops are retrieved successfully!");
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
                .id(userEntity.getId())
                .firstName(first(userEntity.getName()))
                .lastName(last(userEntity.getName()))
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .dateOfBirth(userEntity.getDateOfBirth())
                .profilePhotoUrl(s3FileUtil.generatePresignedUrl(userEntity.getProfilePhotoUrl()))
                .createdAt(userEntity.getCreatedAt())
                .build();
    }

    private AdminShopResponseDto mapToAdminShopDto(AdminShopProjection adminShopProjection) {
        return AdminShopResponseDto.builder()
                .id(adminShopProjection.getId())
                .shopName(adminShopProjection.getShopName())
                .description(adminShopProjection.getDescription())
                .totalIncome(adminShopProjection.getTotalIncome())
                .rating(adminShopProjection.getRating())
                .createdAt(adminShopProjection.getCreatedAt())
                .totalProducts(adminShopProjection.getTotalProducts())
                .subscriptionTier(adminShopProjection.getSubscriptionTier())
                .shopStatus(adminShopProjection.getShopStatus())
                .userEmail(adminShopProjection.getUserEmail())
                .userStatus(adminShopProjection.getUserStatus())
                .build();
    }
}
