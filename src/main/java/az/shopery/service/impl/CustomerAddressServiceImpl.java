package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.AddressRequestDto;
import az.shopery.model.dto.response.AddressResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.AddressEntity;
import az.shopery.model.entity.CustomerEntity;
import az.shopery.repository.CustomerRepository;
import az.shopery.service.CustomerAddressService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerAddressServiceImpl implements CustomerAddressService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public SuccessResponseDto<AddressResponseDto> addAddress(String userEmail, AddressRequestDto addressRequestDto) {
        log.info("Adding address to customer profile for user: {}", userEmail);
        CustomerEntity customerEntity = getCustomerByUserEmail(userEmail);

        boolean isDefault = customerEntity.getAddresses().isEmpty();

        AddressEntity addressEntity = AddressEntity.builder()
                .addressLine1(addressRequestDto.getAddressLine1())
                .addressLine2(addressRequestDto.getAddressLine2())
                .city(addressRequestDto.getCity())
                .country(addressRequestDto.getCountry())
                .postalCode(addressRequestDto.getPostalCode())
                .isDefault(isDefault)
                .customerEntity(customerEntity)
                .build();

        customerEntity.getAddresses().add(addressEntity);
        customerRepository.save(customerEntity);
        log.info("Successfully added new address with ID {} for user {}", addressEntity.getId(), userEmail);

        return SuccessResponseDto.of(mapToAddressResponseDto(addressEntity), "Address added successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<AddressResponseDto> updateAddress(
            String userEmail, UUID addressId, AddressRequestDto addressRequestDto) {
        log.info("Updating address {} for user {}", addressId, userEmail);
        CustomerEntity customerEntity = getCustomerByUserEmail(userEmail);
        AddressEntity addressEntity = customerEntity.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for ID: " + addressId));

        addressEntity.setAddressLine1(addressRequestDto.getAddressLine1());
        addressEntity.setAddressLine2(addressRequestDto.getAddressLine2());
        addressEntity.setCity(addressRequestDto.getCity());
        addressEntity.setCountry(addressRequestDto.getCountry());
        addressEntity.setPostalCode(addressRequestDto.getPostalCode());

        customerRepository.save(customerEntity);
        log.info("Successfully updated address with ID {} for user {}", addressId, userEmail);

        return SuccessResponseDto.of(mapToAddressResponseDto(addressEntity), "Address updated successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> removeAddress(String userEmail, UUID addressId) {
        log.info("Removing address {} for user {}", addressId, userEmail);
        CustomerEntity customerEntity = getCustomerByUserEmail(userEmail);

        boolean removed = customerEntity.getAddresses().removeIf(a -> a.getId().equals(addressId));

        if (!removed) {
            throw new ResourceNotFoundException("Address not found for ID: " + addressId);
        }

        if (customerEntity.getAddresses().stream().noneMatch(AddressEntity::isDefault) &&
                !customerEntity.getAddresses().isEmpty()) {
            customerEntity.getAddresses().getFirst().setDefault(true);
        }

        customerRepository.save(customerEntity);
        log.info("Successfully removed address with ID {} for user {}", addressId, userEmail);

        return SuccessResponseDto.of(null, "Address removed successfully.");
    }

    @Override
    @Transactional
    public SuccessResponseDto<Void> setDefaultAddress(String userEmail, UUID addressId) {
        log.info("Setting default address to {} for user {}", addressId, userEmail);
        CustomerEntity customerEntity = getCustomerByUserEmail(userEmail);

        AddressEntity newDefault = customerEntity.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for ID: " + addressId));

        customerEntity.getAddresses().stream()
                .filter(AddressEntity::isDefault)
                .findFirst()
                .ifPresent(oldDefault -> oldDefault.setDefault(false));

        newDefault.setDefault(true);

        customerRepository.save(customerEntity);
        log.info("Successfully set default address to {} for user {}", addressId, userEmail);

        return SuccessResponseDto.of(null, "Default address has been updated successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponseDto<List<AddressResponseDto>> getAllAddresses(String userEmail) {
        CustomerEntity customerEntity = getCustomerByUserEmail(userEmail);
        List<AddressResponseDto> addresses = customerEntity.getAddresses().stream()
                .map(this::mapToAddressResponseDto)
                .collect(Collectors.toList());
        return SuccessResponseDto.of(addresses, "All addresses retrieved successfully.");
    }

    private AddressResponseDto mapToAddressResponseDto(AddressEntity entity) {
        return AddressResponseDto.builder()
                .id(entity.getId())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .country(entity.getCountry())
                .postalCode(entity.getPostalCode())
                .isDefault(entity.isDefault())
                .build();
    }

    private CustomerEntity getCustomerByUserEmail(String userEmail) {
        return customerRepository.findByUserEntityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for email: " + userEmail));
    }
}
