package az.shopery.service.impl;

import az.shopery.handler.exception.FileStorageException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserRepository;
import az.shopery.service.UserPhotoService;
import az.shopery.utils.aws.FileStorageService;
import java.time.Duration;
import java.util.Objects;

import az.shopery.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserPhotoServiceImpl implements UserPhotoService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    @Transactional
    public SuccessResponse<String> uploadProfilePhoto(String userEmail, MultipartFile multipartFile) {
        String fileKey = fileStorageService.store(multipartFile);
        UserEntity userEntity = getUserByEmail(userEmail);
        userEntity.setProfilePhotoUrl(fileKey);
        userRepository.save(userEntity);
        log.info("Saved profile photo key for {}: {}", userEmail, fileKey);
        return SuccessResponse.of(fileKey, "Profile photo uploaded successfully. User key to get presigned URL.");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<String> generatePresignedUrlForPhoto(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);
        String fileKey = userEntity.getProfilePhotoUrl();
        if (Objects.isNull(fileKey) || fileKey.isBlank()) {
            throw new ResourceNotFoundException("No profile photo found for user: " + userEmail);
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(fileKey).build();
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .getObjectRequest(getObjectRequest)
                    .build();
            String url = s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();
            return SuccessResponse.of(url, "Presigned URL generated successfully.");
        } catch (Exception exception) {
            log.error("Failed to generate presigned URL for file: {}", fileKey, exception);
            throw new FileStorageException("Failed to generate presigned URL.", exception);
        }
    }

    @Override
    @Transactional
    public SuccessResponse<Void> deleteProfilePhoto(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);
        String fileKey = userEntity.getProfilePhotoUrl();
        if (Objects.isNull(fileKey) || fileKey.isBlank()) {
            throw new ResourceNotFoundException("No profile photo found for user: " + userEmail);
        }
        fileStorageService.delete(fileKey);
        userEntity.setProfilePhotoUrl(null);
        userRepository.save(userEntity);
        log.info("Deleted profile photo key for {}: {}", userEmail, fileKey);
        return SuccessResponse.of(null, "Profile photo deleted successfully.");
    }

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
