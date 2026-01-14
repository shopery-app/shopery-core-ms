package az.shopery.service.impl;

import static az.shopery.utils.common.NameMapperHelper.first;
import static az.shopery.utils.common.NameMapperHelper.last;
import static az.shopery.utils.common.UuidUtils.parse;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.CloseMerchantRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.SupportTicketResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.dto.shared.SupportTicketCreatorDto;
import az.shopery.model.entity.OrderEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.SupportTicketEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.OrderRepository;
import az.shopery.repository.SupportTicketRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.AdminService;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.OrderStatus;
import az.shopery.utils.enums.TicketStatus;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final SupportTicketRepository supportTicketRepository;

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

    private UserEntity getAdmin(String userEmail) {
        return userRepository.findByEmailAndUserRoleAndStatus(userEmail, UserRole.ADMIN, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found!"));
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
                .creator(SupportTicketCreatorDto.builder()
                        .id(creator.getId())
                        .name(creator.getName())
                        .email(creator.getEmail())
                        .build())
                .build();
    }
}
