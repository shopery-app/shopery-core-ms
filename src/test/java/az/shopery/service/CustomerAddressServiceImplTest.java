package az.shopery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import az.shopery.handler.exception.AddressLimitExceededException;
import az.shopery.handler.exception.InvalidUuidFormatException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.AddressRequestDto;
import az.shopery.model.entity.AddressEntity;
import az.shopery.model.entity.CustomerEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.CustomerRepository;
import az.shopery.service.impl.CustomerAddressServiceImpl;
import az.shopery.utils.enums.AddressType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerAddressServiceImpl customerAddressService;

    private CustomerEntity customerEntity;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        UserEntity userEntity = UserEntity.builder().email(userEmail).build();
        customerEntity = CustomerEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(userEntity)
                .addresses(new ArrayList<>())
                .build();
    }

    private void mockSaveAndReturn() {
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity savedCustomer = invocation.getArgument(0);
            if (savedCustomer.getAddresses().stream().anyMatch(a -> a.getId() == null)) {
                savedCustomer.getAddresses().stream().filter(a -> a.getId() == null).forEach(a -> a.setId(UUID.randomUUID()));
            }
            return savedCustomer;
        });
    }

    @Nested
    @DisplayName("addAddress Tests")
    class AddAddressTests {

        private AddressRequestDto addressRequestDto;

        @BeforeEach
        void setUp() {
            addressRequestDto = new AddressRequestDto();
            addressRequestDto.setAddressLine1("123 Main St");
            addressRequestDto.setCity("Anytown");
            addressRequestDto.setCountry("US");
            addressRequestDto.setPostalCode("12345");
            addressRequestDto.setAddressType(AddressType.HOUSE);
        }

        @Test
        @DisplayName("should add first address and set it as default")
        void addAddress_firstAddress_shouldBeDefault() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
            mockSaveAndReturn();

            var response = customerAddressService.addAddress(userEmail, addressRequestDto);

            ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
            verify(customerRepository).save(captor.capture());

            assertNotNull(response.getData().getId());
            assertTrue(captor.getValue().getAddresses().getFirst().isDefault());
        }

        @Test
        @DisplayName("should add a new address as non-default when another default exists")
        void addAddress_asNonDefault() {
            AddressEntity oldDefault = AddressEntity.builder().id(UUID.randomUUID()).isDefault(true).build();
            customerEntity.getAddresses().add(oldDefault);

            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
            mockSaveAndReturn();

            customerAddressService.addAddress(userEmail, addressRequestDto);

            ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
            verify(customerRepository).save(captor.capture());

            assertTrue(captor.getValue().getAddresses().getFirst().isDefault());
            assertFalse(captor.getValue().getAddresses().getLast().isDefault());
        }

        @Test
        @DisplayName("should throw AddressLimitExceededException when address limit is reached")
        void addAddress_fail_limitExceeded() {
            for (int i = 0; i < 6; i++) {
                customerEntity.getAddresses().add(AddressEntity.builder().id(UUID.randomUUID()).build());
            }

            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

            assertThrows(AddressLimitExceededException.class, () ->
                    customerAddressService.addAddress(userEmail, addressRequestDto));

            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateAddress Tests")
    class UpdateAddressTests {
        private AddressRequestDto addressRequestDto;
        private AddressEntity existingAddress;
        private String validAddressId;

        @BeforeEach
        void setUp() {
            addressRequestDto = new AddressRequestDto();
            addressRequestDto.setAddressLine1("456 New Ave");
            addressRequestDto.setCity("Newville");
            addressRequestDto.setCountry("CA");
            addressRequestDto.setPostalCode("A1A 1A1");
            addressRequestDto.setAddressType(AddressType.OFFICE);

            existingAddress = AddressEntity.builder()
                    .id(UUID.randomUUID())
                    .addressLine1("123 Old St")
                    .city("Oldtown")
                    .addressType(AddressType.HOUSE)
                    .build();

            customerEntity.getAddresses().add(existingAddress);
            validAddressId = existingAddress.getId().toString();
        }

        @Test
        @DisplayName("should update address successfully")
        void updateAddress_success() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
            when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customerEntity);

            var response = customerAddressService.updateAddress(userEmail, validAddressId, addressRequestDto);

            verify(customerRepository).save(customerEntity);
            assertEquals("456 New Ave", existingAddress.getAddressLine1());
            assertEquals("Newville", existingAddress.getCity());
            assertEquals(AddressType.OFFICE, existingAddress.getAddressType());
            assertEquals(existingAddress.getId(), response.getData().getId());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for non-existent address ID")
        void updateAddress_fail_addressNotFound() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
            String nonExistentId = UUID.randomUUID().toString();

            assertThrows(ResourceNotFoundException.class, () ->
                    customerAddressService.updateAddress(userEmail, nonExistentId, addressRequestDto));
        }

        @Test
        @DisplayName("should throw InvalidUuidFormatException for invalid ID format")
        void updateAddress_fail_invalidUuid() {
            assertThrows(InvalidUuidFormatException.class, () ->
                    customerAddressService.updateAddress(userEmail, "not-a-uuid", addressRequestDto));
        }
    }

    @Nested
    @DisplayName("removeAddress Tests")
    class RemoveAddressTests {
        private AddressEntity address1, address2;
        private String addressId1, addressId2;

        @BeforeEach
        void setUp() {
            address1 = AddressEntity.builder().id(UUID.randomUUID()).isDefault(true).build();
            address2 = AddressEntity.builder().id(UUID.randomUUID()).isDefault(false).build();
            customerEntity.getAddresses().addAll(List.of(address1, address2));
            addressId1 = address1.getId().toString();
            addressId2 = address2.getId().toString();
        }

        @Test
        @DisplayName("should remove address successfully")
        void removeAddress_success() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

            customerAddressService.removeAddress(userEmail, addressId2);

            ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
            verify(customerRepository).save(captor.capture());

            assertEquals(1, captor.getValue().getAddresses().size());
            assertTrue(captor.getValue().getAddresses().getFirst().isDefault());
        }

        @Test
        @DisplayName("should remove default address and assign new default")
        void removeAddress_defaultAddress_assignsNewDefault() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

            customerAddressService.removeAddress(userEmail, addressId1);

            ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
            verify(customerRepository).save(captor.capture());

            assertEquals(1, captor.getValue().getAddresses().size());
            assertTrue(captor.getValue().getAddresses().getFirst().isDefault());
            assertEquals(address2.getId(), captor.getValue().getAddresses().getFirst().getId());
        }

        @Test
        @DisplayName("should remove last address and not assign a new default")
        void removeAddress_lastAddress() {
            customerEntity.setAddresses(new ArrayList<>(List.of(address1)));
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

            customerAddressService.removeAddress(userEmail, addressId1);

            ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
            verify(customerRepository).save(captor.capture());

            assertTrue(captor.getValue().getAddresses().isEmpty());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for non-existent address ID")
        void removeAddress_fail_addressNotFound() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
            String nonExistentId = UUID.randomUUID().toString();

            assertThrows(ResourceNotFoundException.class, () ->
                    customerAddressService.removeAddress(userEmail, nonExistentId));
        }

        @Test
        @DisplayName("should throw InvalidUuidFormatException for invalid ID format")
        void removeAddress_fail_invalidUuid() {
            assertThrows(InvalidUuidFormatException.class, () ->
                    customerAddressService.removeAddress(userEmail, "not-a-uuid"));
        }
    }

    @Nested
    @DisplayName("setDefaultAddress Tests")
    class SetDefaultAddressTests {
        private AddressEntity oldDefault, newDefault;
        private String newDefaultId;

        @BeforeEach
        void setUp() {
            oldDefault = AddressEntity.builder().id(UUID.randomUUID()).isDefault(true).build();
            newDefault = AddressEntity.builder().id(UUID.randomUUID()).isDefault(false).build();
            customerEntity.getAddresses().addAll(List.of(oldDefault, newDefault));
            newDefaultId = newDefault.getId().toString();
        }

        @Test
        @DisplayName("should set new default and unset old default")
        void setDefaultAddress_success() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

            customerAddressService.setDefaultAddress(userEmail, newDefaultId);

            ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
            verify(customerRepository).save(captor.capture());

            CustomerEntity savedCustomer = captor.getValue();
            assertFalse(savedCustomer.getAddresses().stream().anyMatch(a -> a.getId().equals(oldDefault.getId()) && a.isDefault()));
            assertTrue(savedCustomer.getAddresses().stream().anyMatch(a -> a.getId().equals(newDefault.getId()) && a.isDefault()));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for non-existent address ID")
        void setDefaultAddress_fail_addressNotFound() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
            String nonExistentId = UUID.randomUUID().toString();

            assertThrows(ResourceNotFoundException.class, () ->
                    customerAddressService.setDefaultAddress(userEmail, nonExistentId));
        }

        @Test
        @DisplayName("should throw InvalidUuidFormatException for invalid ID format")
        void setDefaultAddress_fail_invalidUuid() {
            assertThrows(InvalidUuidFormatException.class, () ->
                    customerAddressService.setDefaultAddress(userEmail, "not-a-uuid"));
        }
    }

    @Nested
    @DisplayName("getAllAddresses Tests")
    class GetAllAddressesTests {
        @Test
        @DisplayName("should return a list of addresses")
        void getAllAddresses_success() {
            customerEntity.getAddresses().add(AddressEntity.builder().id(UUID.randomUUID()).build());
            customerEntity.getAddresses().add(AddressEntity.builder().id(UUID.randomUUID()).build());

            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

            var response = customerAddressService.getAllAddresses(userEmail);

            assertEquals(2, response.getData().size());
        }

        @Test
        @DisplayName("should return an empty list for user with no addresses")
        void getAllAddresses_emptyList() {
            when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

            var response = customerAddressService.getAllAddresses(userEmail);

            assertTrue(response.getData().isEmpty());
        }
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException for non-existent customer")
    void getCustomerByUserEmail_fail_customerNotFound() {
        when(customerRepository.findByUserEntityEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                customerAddressService.getAllAddresses(userEmail));
    }
}
