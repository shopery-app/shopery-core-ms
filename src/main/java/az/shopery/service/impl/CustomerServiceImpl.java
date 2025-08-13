package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.CustomerProfileUpdateRequestDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.response.CustomerProfileResponseDto;
import az.shopery.model.dto.response.MerchantProfileResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.CustomerAddressEntity;
import az.shopery.model.entity.CustomerEntity;
import az.shopery.model.entity.MerchantAddressEntity;
import az.shopery.model.entity.MerchantEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.CustomerRepository;
import az.shopery.repository.MerchantRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.CustomerService;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.security.JwtService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void createCustomerProfile(UserEntity userEntity) {
        String fullName = userEntity.getName();
        String[] names = fullName.trim().split("\\s+");
        CustomerEntity customerEntity = CustomerEntity.builder()
                .userEntity(userEntity)
                .firstName(names[0].trim())
                .lastName(names[1].trim())
                .build();
        customerRepository.save(customerEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<CustomerProfileResponseDto> getCustomerProfile(String userEmail) {
        CustomerEntity customerEntity = customerRepository.findByUserEntityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer profile not found for email: " + userEmail));

        var customerResponse = mapToCustomerProfileResponseDto(customerEntity);

        return SuccessResponseDto.of(customerResponse, "Customer profile retrieved successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<CustomerProfileResponseDto> updateCustomerProfile(String userEmail, CustomerProfileUpdateRequestDto customerProfileUpdateRequestDto) {
        CustomerEntity customerEntity = customerRepository.findByUserEntityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer profile not found for email: " + userEmail));

        customerEntity.setFirstName(customerProfileUpdateRequestDto.getFirstName());
        customerEntity.setLastName(customerProfileUpdateRequestDto.getLastName());
        customerEntity.setPhone(customerProfileUpdateRequestDto.getPhone());
        customerEntity.setDateOfBirth(customerProfileUpdateRequestDto.getDateOfBirth());

        CustomerEntity updatedCustomerEntity = customerRepository.save(customerEntity);
        log.info("Updated customer profile for user {}", userEmail);

        var customerResponse = mapToCustomerProfileResponseDto(updatedCustomerEntity);

        return SuccessResponseDto.of(customerResponse, "Customer profile updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<BecomeMerchantResponseDto> becomeMerchant(String userEmail) {
        CustomerEntity customerEntity = customerRepository.findByUserEntityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer profile not found for email: " + userEmail));

        List<CustomerAddressEntity> addressesToTransfer = new ArrayList<>(customerEntity.getCustomerAddresses());

        UserEntity userEntity = customerEntity.getUserEntity();
        userEntity.setUserRole(UserRole.MERCHANT);
        userRepository.save(userEntity);
        log.info("User {} role updated to MERCHANT.", userEmail);

        MerchantEntity merchantEntity = MerchantEntity.builder()
                .userEntity(userEntity)
                .firstName(customerEntity.getFirstName())
                .lastName(customerEntity.getLastName())
                .dateOfBirth(customerEntity.getDateOfBirth())
                .phone(customerEntity.getPhone())
                .profilePhotoUrl(customerEntity.getProfilePhotoUrl())
                .build();

        List<MerchantAddressEntity> newMerchantAddresses = addressesToTransfer.stream()
                .map(oldAddress -> MerchantAddressEntity.builder()
                        .addressType(oldAddress.getAddressType())
                        .addressLine1(oldAddress.getAddressLine1())
                        .addressLine2(oldAddress.getAddressLine2())
                        .city(oldAddress.getCity())
                        .country(oldAddress.getCountry())
                        .postalCode(oldAddress.getPostalCode())
                        .isDefault(oldAddress.isDefault())
                        .merchantEntity(merchantEntity)
                        .build())
                .toList();
        merchantEntity.setMerchantAddresses(newMerchantAddresses);
        merchantRepository.save(merchantEntity);
        customerRepository.delete(customerEntity);
        log.info("Converted customer profile to merchant profile for user {}", userEmail);

        var userDetails = User.withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(userEntity.getUserRole().name())
                .build();

        var accessToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        var merchantProfileResponseDto = mapToMerchantProfileResponseDto(merchantEntity);

        var becomeMerchantResponseDto = BecomeMerchantResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .merchantProfileResponseDto(merchantProfileResponseDto)
                .build();
        return SuccessResponseDto.of(becomeMerchantResponseDto, "Become merchant successfully.");
    }

    private CustomerProfileResponseDto mapToCustomerProfileResponseDto(CustomerEntity customerEntity) {
        return CustomerProfileResponseDto.builder()
                .firstName(customerEntity.getFirstName())
                .lastName(customerEntity.getLastName())
                .email(customerEntity.getUserEntity().getEmail())
                .phone(customerEntity.getPhone())
                .dateOfBirth(customerEntity.getDateOfBirth())
                .profilePhotoUrl(customerEntity.getProfilePhotoUrl())
                .createdAt(customerEntity.getCreatedAt())
                .build();
    }

    private MerchantProfileResponseDto mapToMerchantProfileResponseDto(MerchantEntity merchantEntity) {
        return MerchantProfileResponseDto.builder()
                .firstName(merchantEntity.getFirstName())
                .lastName(merchantEntity.getLastName())
                .email(merchantEntity.getUserEntity().getEmail())
                .phone(merchantEntity.getPhone())
                .dateOfBirth(merchantEntity.getDateOfBirth())
                .profilePhotoUrl(merchantEntity.getProfilePhotoUrl())
                .createdAt(merchantEntity.getCreatedAt())
                .build();
    }
}
