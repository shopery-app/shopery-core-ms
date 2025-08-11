package az.shopery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.AddressRequestDto;
import az.shopery.model.dto.response.AddressResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.AddressEntity;
import az.shopery.model.entity.CustomerEntity;
import az.shopery.repository.CustomerRepository;
import az.shopery.service.impl.CustomerAddressServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerAddressServiceImpl customerAddressService;

    private CustomerEntity customerEntity;
    private AddressRequestDto addressRequestDto;
    private final String userEmail = "test@example.com";
    private final UUID addressId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        customerAddressService = new CustomerAddressServiceImpl(customerRepository);
        customerEntity = new CustomerEntity();
        customerEntity.setId(UUID.randomUUID());
        customerEntity.setAddresses(new ArrayList<>());
        addressRequestDto = AddressRequestDto.builder()
                .addressLine1("123 Main St")
                .city("Anytown")
                .country("Country")
                .postalCode("12345")
                .build();
    }

    @Test
    void addAddress_shouldMakeFirstAddressDefault() {
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        SuccessResponseDto<AddressResponseDto> response = customerAddressService.addAddress(userEmail, addressRequestDto);
        assertTrue(response.getData().isDefault());
    }

    @Test
    void addAddress_shouldNotMakeSecondAddressDefault() {
        AddressEntity existingDefaultAddress = AddressEntity.builder()
                .id(UUID.randomUUID())
                .isDefault(true)
                .build();
        customerEntity.getAddresses().add(existingDefaultAddress);
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        SuccessResponseDto<AddressResponseDto> response = customerAddressService.addAddress(userEmail, addressRequestDto);
        assertFalse(response.getData().isDefault());
        assertTrue(existingDefaultAddress.isDefault());
    }

    @Test
    void updateAddress_shouldUpdateExistingAddress() {
        AddressEntity addressEntity = AddressEntity.builder().id(addressId).build();
        customerEntity.getAddresses().add(addressEntity);
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        SuccessResponseDto<AddressResponseDto> response = customerAddressService.updateAddress(userEmail, addressId, addressRequestDto);
        assertEquals("123 Main St", response.getData().getAddressLine1());
    }

    @Test
    void removeAddress_shouldPromoteNewDefault_whenDefaultIsRemoved() {
        AddressEntity addressToRemove = AddressEntity.builder()
                .id(addressId)
                .isDefault(true)
                .build();
        AddressEntity addressToPromote = AddressEntity.builder()
                .id(UUID.randomUUID())
                .isDefault(false)
                .build();
        customerEntity.getAddresses().addAll(List.of(addressToRemove, addressToPromote));
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        customerAddressService.removeAddress(userEmail, addressId);
        assertTrue(customerEntity.getAddresses().getFirst().isDefault());
    }

    @Test
    void removeAddress_shouldSkipPromotion_whenDefaultAddressRemains() {
        AddressEntity defaultAddress = AddressEntity.builder()
                .id(UUID.randomUUID())
                .isDefault(true)
                .build();
        AddressEntity nonDefaultToRemove = AddressEntity.builder().id(addressId).isDefault(false).build();
        customerEntity.getAddresses().addAll(List.of(defaultAddress, nonDefaultToRemove));
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        customerAddressService.removeAddress(userEmail, addressId);
        assertEquals(1, customerEntity.getAddresses().size());
        assertTrue(defaultAddress.isDefault());
    }

    @Test
    void setDefaultAddress_shouldSetNewDefaultAndUnsetOldOne() {
        AddressEntity oldDefault = AddressEntity.builder()
                .id(UUID.randomUUID())
                .isDefault(true)
                .build();
        AddressEntity newDefault = AddressEntity.builder().id(addressId).isDefault(false).build();
        customerEntity.getAddresses().addAll(List.of(oldDefault, newDefault));
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        customerAddressService.setDefaultAddress(userEmail, addressId);
        assertFalse(oldDefault.isDefault());
        assertTrue(newDefault.isDefault());
    }

    @Test
    void setDefaultAddress_shouldSucceedWhenNoPreviousDefaultExists() {
        AddressEntity addressToMakeDefault = AddressEntity.builder().id(addressId).isDefault(false).build();
        customerEntity.getAddresses().add(addressToMakeDefault);
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        customerAddressService.setDefaultAddress(userEmail, addressId);
        assertTrue(addressToMakeDefault.isDefault());
    }

    @Test
    void getAllAddresses_shouldReturnAllCustomerAddressDtos() {
        customerEntity.getAddresses().add(AddressEntity.builder().addressLine1("123 Main St").build());
        customerEntity.getAddresses().add(AddressEntity.builder().addressLine1("456 Side St").build());
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        SuccessResponseDto<List<AddressResponseDto>> response = customerAddressService.getAllAddresses(userEmail);
        assertNotNull(response);
        assertEquals(2, response.getData().size());
        assertEquals("123 Main St", response.getData().getFirst().getAddressLine1());
    }

    @Test
    void getCustomerByUserEmail_shouldThrowException_whenCustomerNotFound() {
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customerAddressService.getAllAddresses(userEmail));
    }

    @Test
    void updateAddress_shouldThrowResourceNotFoundException_whenAddressNotFound() {
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        assertThrows(ResourceNotFoundException.class, () -> customerAddressService.updateAddress(userEmail, UUID.randomUUID(), addressRequestDto));
    }

    @Test
    void removeAddress_shouldThrowException_whenAddressNotFound() {
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        assertThrows(ResourceNotFoundException.class, () -> customerAddressService.removeAddress(userEmail, addressId));
    }

    @Test
    void removeAddress_shouldSucceed_whenLastAddressIsRemoved() {
        AddressEntity lastAddress = AddressEntity.builder()
                .id(addressId)
                .isDefault(true)
                .build();
        customerEntity.getAddresses().add(lastAddress);
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        customerAddressService.removeAddress(userEmail, addressId);
        assertTrue(customerEntity.getAddresses().isEmpty());
    }

    @Test
    void setDefaultAddress_shouldThrowException_whenAddressNotFound() {
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        assertThrows(ResourceNotFoundException.class, () -> customerAddressService.setDefaultAddress(userEmail, addressId));
    }
}
