package az.shopery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import az.shopery.handler.exception.FileStorageException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.CustomerEntity;
import az.shopery.repository.CustomerRepository;
import az.shopery.service.impl.CustomerPhotoServiceImpl;
import az.shopery.utils.aws.FileStorageService;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class CustomerPhotoServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private S3Presigner s3Presigner;

    private CustomerPhotoServiceImpl customerPhotoService;

    private final String userEmail = "test@example.com";
    private final String fileKey = "unique-file-key.jpg";

    @BeforeEach
    void setUp() {
        customerPhotoService = new CustomerPhotoServiceImpl(customerRepository, fileStorageService, s3Presigner);
        ReflectionTestUtils.setField(customerPhotoService, "bucketName", "test-bucket");
    }

    @Test
    void uploadProfilePhoto_shouldSucceed() {
        MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        CustomerEntity customerEntity = new CustomerEntity();
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        when(fileStorageService.store(multipartFile)).thenReturn(fileKey);

        SuccessResponseDto<String> response = customerPhotoService.uploadProfilePhoto(userEmail, multipartFile);

        assertNotNull(response);
        assertEquals(fileKey, response.getData());
        assertEquals(fileKey, customerEntity.getProfilePhotoUrl());
        verify(customerRepository, times(1)).save(customerEntity);
        verify(fileStorageService, times(1)).store(multipartFile);
    }

    @Test
    void uploadProfilePhoto_shouldThrowResourceNotFoundException_whenCustomerNotFound() {
        MultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        when(fileStorageService.store(multipartFile)).thenReturn(fileKey);
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerPhotoService.uploadProfilePhoto(userEmail, multipartFile));
        verify(customerRepository, never()).save(any(CustomerEntity.class));
    }

    @Test
    void generatePresignedUrlForPhoto_shouldSucceed() throws Exception {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setProfilePhotoUrl(fileKey);
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

        String expectedUrl = "https://test-bucket.s3.amazonaws.com/" + fileKey;
        URL url = new URI(expectedUrl).toURL();
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(url);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        SuccessResponseDto<String> response = customerPhotoService.generatePresignedUrlForPhoto(userEmail);

        assertNotNull(response);
        assertEquals(expectedUrl, response.getData());
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void generatePresignedUrlForPhoto_shouldThrowResourceNotFoundException_whenNoPhotoUrl() {
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(new CustomerEntity()));

        assertThrows(ResourceNotFoundException.class, () -> customerPhotoService.generatePresignedUrlForPhoto(userEmail));
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void generatePresignedUrlForPhoto_shouldThrowResourceNotFoundException_whenPhotoUrlIsBlank() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setProfilePhotoUrl("  ");
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

        assertThrows(ResourceNotFoundException.class, () -> customerPhotoService.generatePresignedUrlForPhoto(userEmail));
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void generatePresignedUrlForPhoto_shouldThrowFileStorageException_whenPreSignerFails() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setProfilePhotoUrl(fileKey);
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenThrow(new RuntimeException("AWS SDK Error"));

        assertThrows(FileStorageException.class, () -> customerPhotoService.generatePresignedUrlForPhoto(userEmail));
    }

    @Test
    void deleteProfilePhoto_shouldSucceed_whenPhotoExists() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setProfilePhotoUrl(fileKey);
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

        customerPhotoService.deleteProfilePhoto(userEmail);

        assertNull(customerEntity.getProfilePhotoUrl());
        verify(fileStorageService, times(1)).delete(fileKey);
        verify(customerRepository, times(1)).save(customerEntity);
    }

    @Test
    void deleteProfilePhoto_shouldSucceedAndDoNothing_whenNoPhotoExists() {
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(new CustomerEntity()));

        SuccessResponseDto<Void> response = customerPhotoService.deleteProfilePhoto(userEmail);

        assertEquals("User does not have a profile photo.", response.getMessage());
        verify(fileStorageService, never()).delete(anyString());
        verify(customerRepository, never()).save(any(CustomerEntity.class));
    }

    @Test
    void deleteProfilePhoto_shouldSucceed_whenPhotoUrlIsBlank() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setProfilePhotoUrl("   ");
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.of(customerEntity));

        SuccessResponseDto<Void> response = customerPhotoService.deleteProfilePhoto(userEmail);

        assertEquals("User does not have a profile photo.", response.getMessage());
        verify(fileStorageService, never()).delete(anyString());
        verify(customerRepository, never()).save(any(CustomerEntity.class));
    }

    @Test
    void getCustomerByUserEmail_shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findByUserEntityEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerPhotoService.deleteProfilePhoto(userEmail));
    }
}