package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.CloseMerchantRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.entity.OrderEntity;
import az.shopery.model.entity.ShopEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.OrderRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.AdminService;
import az.shopery.service.EmailService;
import az.shopery.utils.enums.OrderStatus;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import static az.shopery.utils.common.NameMapperHelper.first;
import static az.shopery.utils.common.NameMapperHelper.last;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Override
    public SuccessResponseDto<Page<UserProfileResponseDto>> getCustomers(Pageable pageable) {
        Page<UserEntity> customers = userRepository.findAllByUserRoleAndStatus(UserRole.CUSTOMER, UserStatus.ACTIVE, pageable);
        return SuccessResponseDto.of(customers.map(this::mapToDto), "Customers are retrieved successfully");
    }

    @Override
    public SuccessResponseDto<Page<UserProfileResponseDto>> getMerchants(Pageable pageable) {
        Page<UserEntity> customers = userRepository.findAllByUserRoleAndStatus(UserRole.MERCHANT, UserStatus.ACTIVE, pageable);
        return SuccessResponseDto.of(customers.map(this::mapToDto), "Merchants are retrieved successfully");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> closeMerchant(CloseMerchantRequestDto closeMerchantRequestDto) {
        String email = closeMerchantRequestDto.getEmail();
        UserEntity user = userRepository.findByEmailAndUserRoleAndStatus(email, UserRole.MERCHANT, UserStatus.ACTIVE).orElseThrow(
                () -> new ResourceNotFoundException("Merchant not found")
        );
        user.setStatus(UserStatus.CLOSED);

        ShopEntity shop = user.getShop();
        List<OrderEntity> orders = orderRepository.findAllByShopId(shop.getId());
        orders.forEach(order -> {
           UserEntity customer = order.getUser();
           emailService.sendMerchantClosedNotification(customer.getEmail(), customer.getName(), user.getName());
           order.setStatus(OrderStatus.CANCELLED);
        });

        return SuccessResponseDto.of("User deleted successfully");
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
