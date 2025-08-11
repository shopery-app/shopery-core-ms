package az.shopery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.CustomerProfileUpdateRequestDto;
import az.shopery.model.dto.response.CustomerProfileResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.CustomerEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.CustomerRepository;
import az.shopery.service.impl.CustomerServiceImpl;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Captor
    private ArgumentCaptor<CustomerEntity> customerEntityCaptor;

    @Test
    void createCustomerProfile_shouldSplitNameAndSaveCustomer() {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("John Doe");

        customerService.createCustomerProfile(userEntity);

        verify(customerRepository, times(1)).save(customerEntityCaptor.capture());
        CustomerEntity savedCustomer = customerEntityCaptor.getValue();

        assertEquals("John", savedCustomer.getFirstName());
        assertEquals("Doe", savedCustomer.getLastName());
        assertEquals(userEntity, savedCustomer.getUserEntity());
    }

    @Test
    void createCustomerProfile_shouldHandleExtraSpacesInName() {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("  Jane   Doe  ");

        customerService.createCustomerProfile(userEntity);

        verify(customerRepository, times(1)).save(customerEntityCaptor.capture());
        CustomerEntity savedCustomer = customerEntityCaptor.getValue();

        assertEquals("Jane", savedCustomer.getFirstName());
        assertEquals("Doe", savedCustomer.getLastName());
    }

    @Test
    void createCustomerProfile_shouldThrowException_forSingleName() {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("Cher");

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> customerService.createCustomerProfile(userEntity));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void getCustomerProfile_shouldReturnProfile_whenCustomerExists() {
        String userEmail = "test@example.com";
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(userEmail);

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setFirstName("John");
        customerEntity.setLastName("Doe");
        customerEntity.setUserEntity(userEntity);

        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

        SuccessResponseDto<CustomerProfileResponseDto> response = customerService.getCustomerProfile(userEmail);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("John", response.getData().getFirstName());
        assertEquals("Doe", response.getData().getLastName());
        assertEquals(userEmail, response.getData().getEmail());
    }

    @Test
    void getCustomerProfile_shouldThrowResourceNotFoundException_whenCustomerDoesNotExist() {
        String userEmail = "notfound@example.com";
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerProfile(userEmail));
    }

    @Test
    void updateCustomerProfile_shouldSucceed_whenCustomerExists() {
        String userEmail = "test@example.com";
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(userEmail);

        CustomerEntity existingCustomer = new CustomerEntity();
        existingCustomer.setFirstName("OldFirst");
        existingCustomer.setLastName("OldLast");
        existingCustomer.setUserEntity(userEntity);

        LocalDate localDate = LocalDate.of(2000, 1, 1);
        Date expectedDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        CustomerProfileUpdateRequestDto updateRequest = new CustomerProfileUpdateRequestDto();
        updateRequest.setFirstName("NewFirst");
        updateRequest.setLastName("NewLast");
        updateRequest.setPhone("123456789");
        updateRequest.setDateOfBirth(expectedDate);

        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SuccessResponseDto<CustomerProfileResponseDto> response = customerService.updateCustomerProfile(userEmail, updateRequest);

        verify(customerRepository, times(1)).save(customerEntityCaptor.capture());
        CustomerEntity savedCustomer = customerEntityCaptor.getValue();

        assertEquals("NewFirst", savedCustomer.getFirstName());
        assertEquals("NewLast", savedCustomer.getLastName());
        assertEquals("123456789", savedCustomer.getPhone());
        assertEquals(expectedDate, savedCustomer.getDateOfBirth());

        assertNotNull(response.getData());
        assertEquals("NewFirst", response.getData().getFirstName());
    }

    @Test
    void updateCustomerProfile_shouldThrowResourceNotFoundException_whenCustomerDoesNotExist() {
        String userEmail = "notfound@example.com";
        CustomerProfileUpdateRequestDto updateRequest = new CustomerProfileUpdateRequestDto();
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomerProfile(userEmail, updateRequest));

        verify(customerRepository, never()).save(any());
    }
}
