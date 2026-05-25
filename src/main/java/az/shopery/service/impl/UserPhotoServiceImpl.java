package az.shopery.service.impl;

import az.shopery.client.AwsClient;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserRepository;
import az.shopery.service.UserPhotoService;
import az.shopery.utils.enums.UserStatus;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPhotoServiceImpl implements UserPhotoService {

    private final AwsClient awsClient;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SuccessResponse<String> uploadProfilePhoto(String userEmail, MultipartFile multipartFile) {
        String fileKey = awsClient.uploadFile(multipartFile).getBody();

        UserEntity userEntity = getUserByEmail(userEmail);
        userEntity.setProfilePhotoUrl(fileKey);
        userRepository.save(userEntity);

        String presignedUrl = awsClient.getPresignedUrl(fileKey).getBody();

        log.info("Saved profile photo key for {}: {}", userEmail, fileKey);
        return SuccessResponse.of(presignedUrl, "Profile photo uploaded successfully. User key to get presigned URL.");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> deleteProfilePhoto(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);
        String fileKey = userEntity.getProfilePhotoUrl();
        if (Objects.isNull(fileKey) || fileKey.isBlank()) {
            throw new ResourceNotFoundException("No profile photo found for user: " + userEmail);
        }

        awsClient.deleteFile(fileKey);
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
