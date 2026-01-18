package az.shopery.service.impl;

import static az.shopery.utils.common.NameMapperHelper.first;
import static az.shopery.utils.common.NameMapperHelper.last;
import static az.shopery.utils.common.UuidUtils.parse;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.event.ShopCreationRequestApprovedEvent;
import az.shopery.model.dto.event.ShopCreationRequestRejectedEvent;
import az.shopery.model.dto.request.CloseMerchantRequestDto;
import az.shopery.model.dto.request.ShopCreationRequestRejectDto;
import az.shopery.model.dto.response.ShopCreationRequestResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.SupportTicketResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.dto.shared.TaskCreatorDto;
import az.shopery.model.entity.OrderEntity;
import az.shopery.model.entity.ShopCreationRequestEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.SupportTicketEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.OrderRepository;
import az.shopery.repository.ShopRepository;
import az.shopery.repository.admin.ShopCreationRequestRepository;
import az.shopery.repository.admin.SupportTicketRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.AdminService;
import az.shopery.utils.enums.OrderStatus;
import az.shopery.utils.enums.RequestStatus;
import az.shopery.utils.enums.TicketStatus;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ShopCreationRequestRepository shopCreationRequestRepository;
    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public SuccessResponseDto<Page<UserProfileResponseDto>> getCustomers(Pageable pageable) {
        Page<UserEntity> customers = userRepository.findAllByUserRoleAndStatus(UserRole.CUSTOMER, UserStatus.ACTIVE, pageable);
        return SuccessResponseDto.of(customers.map(this::mapToDto), "Customers are retrieved successfully!");
    }

    @Override
    public SuccessResponseDto<Page<UserProfileResponseDto>> getMerchants(Pageable pageable) {
        Page<UserEntity> customers = userRepository.findAllByUserRoleAndStatus(UserRole.MERCHANT, UserStatus.ACTIVE, pageable);
        return SuccessResponseDto.of(customers.map(this::mapToDto), "Merchants are retrieved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> closeMerchant(CloseMerchantRequestDto closeMerchantRequestDto) {
        String email = closeMerchantRequestDto.getEmail();
        UserEntity user = userRepository.findByEmailAndUserRoleAndStatus(email, UserRole.MERCHANT, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found!"));
        user.setStatus(UserStatus.CLOSED);

        ShopEntity shop = user.getShop();
        List<OrderEntity> orders = orderRepository.findAllByShopId(shop.getId());
        for (OrderEntity order : orders) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        return SuccessResponseDto.of("User deleted successfully!");
    }

    @Override
    public SuccessResponseDto<Page<SupportTicketResponseDto>> getSupportTickets(Pageable pageable, String userEmail) {
        Page<SupportTicketEntity> supportTicketEntities = supportTicketRepository.getAllSupportTicketsByAssignedAdmin(getAdmin(userEmail), pageable);
        return SuccessResponseDto.of(supportTicketEntities.map(this::mapToSupportTicketResponseDto), "Support tickets are retrieved successfully!");
    }

    @Override
    public SuccessResponseDto<Void> closeSupportTicket(String id, String userEmail) {
        SupportTicketEntity supportTicketEntity = supportTicketRepository.findByIdAndAssignedAdmin(parse(id), getAdmin(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found!"));
        supportTicketEntity.setStatus(TicketStatus.CLOSED);
        supportTicketRepository.save(supportTicketEntity);
        return SuccessResponseDto.of("Support ticket has been closed successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> approve(String id, String userEmail) {
        ShopCreationRequestEntity shopCreationRequestEntity = getShopCreationRequestEntity(id, userEmail);

        ShopEntity shop = ShopEntity.builder()
                .user(shopCreationRequestEntity.getCreatedBy())
                .shopName(shopCreationRequestEntity.getShopName())
                .description(shopCreationRequestEntity.getDescription())
                .totalIncome(BigDecimal.ZERO)
                .rating(0.0)
                .build();

        shopRepository.save(shop);
        shopCreationRequestEntity.setStatus(RequestStatus.APPROVED);
        applicationEventPublisher.publishEvent(new ShopCreationRequestApprovedEvent(shopCreationRequestEntity));

        return SuccessResponseDto.of("Shop creation request has been approved successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> reject(String id, String userEmail, ShopCreationRequestRejectDto shopCreationRequestRejectDto) {
        ShopCreationRequestEntity shopCreationRequestEntity = getShopCreationRequestEntity(id, userEmail);

        shopCreationRequestEntity.setStatus(RequestStatus.REJECTED);
        shopCreationRequestEntity.setRejectionReason(shopCreationRequestRejectDto.getReason());
        applicationEventPublisher.publishEvent(new ShopCreationRequestRejectedEvent(shopCreationRequestEntity));

        return SuccessResponseDto.of("Shop creation request has been rejected successfully!");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Page<ShopCreationRequestResponseDto>> getShopCreationRequestsByAssignedAdmin(String userEmail, Pageable pageable) {
        Page<ShopCreationRequestEntity> shopCreationRequestEntities = shopCreationRequestRepository.findByAssignedAdminAndStatus(getAdmin(userEmail), RequestStatus.PENDING, pageable);
        return SuccessResponseDto.of(shopCreationRequestEntities.map(this::mapToShopCreationRequestResponseDto),
                "Shop creation requests retrieved successfully for this admin!");
    }

    private UserEntity getAdmin(String userEmail) {
        return userRepository.findByEmailAndUserRoleAndStatus(userEmail, UserRole.ADMIN, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found!"));
    }

    private ShopCreationRequestEntity getShopCreationRequestEntity(String id, String userEmail) {
        UserEntity admin = getAdmin(userEmail);
        return shopCreationRequestRepository.findByIdAndAssignedAdminAndStatus(parse(id), admin, RequestStatus.PENDING)
                .orElseThrow(() -> new  ResourceNotFoundException("Shop creation request not found!"));
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

    private SupportTicketResponseDto mapToSupportTicketResponseDto(SupportTicketEntity supportTicketEntity) {
        UserEntity creator = supportTicketEntity.getCreatedBy();
        return SupportTicketResponseDto.builder()
                .id(supportTicketEntity.getId())
                .subject(supportTicketEntity.getSubject())
                .description(supportTicketEntity.getDescription())
                .status(supportTicketEntity.getStatus())
                .createdAt(supportTicketEntity.getCreatedAt())
                .updatedAt(supportTicketEntity.getUpdatedAt())
                .creator(TaskCreatorDto.builder()
                        .id(creator.getId())
                        .name(creator.getName())
                        .email(creator.getEmail())
                        .build())
                .build();
    }

    private ShopCreationRequestResponseDto mapToShopCreationRequestResponseDto (ShopCreationRequestEntity shopCreationRequestEntity) {
        UserEntity creator = shopCreationRequestEntity.getCreatedBy();
        return ShopCreationRequestResponseDto.builder()
                .id(shopCreationRequestEntity.getId())
                .creator(TaskCreatorDto.builder()
                        .id(creator.getId())
                        .name(creator.getName())
                        .email(creator.getEmail())
                        .build())
                .createdAt(shopCreationRequestEntity.getCreatedAt())
                .build();
    }
}
