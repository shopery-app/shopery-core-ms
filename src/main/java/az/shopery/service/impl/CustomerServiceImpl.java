package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.response.CustomerProfileResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.CustomerEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.CustomerRepository;
import az.shopery.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

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

        var customerResponse = CustomerProfileResponseDto.builder()
                .firstName(customerEntity.getFirstName())
                .lastName(customerEntity.getLastName())
                .email(customerEntity.getUserEntity().getEmail())
                .phone(customerEntity.getPhone())
                .dateOfBirth(customerEntity.getDateOfBirth())
                .profilePhotoUrl(customerEntity.getProfilePhotoUrl())
                .createdAt(customerEntity.getCreatedAt())
                .build();

        return SuccessResponseDto.of(customerResponse, "Customer profile retrieved successfully.");
    }
}
