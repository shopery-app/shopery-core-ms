package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserRepository;
import az.shopery.service.UserPhotoService;
import az.shopery.utils.aws.FileStorageService;
import java.util.Objects;
import az.shopery.utils.aws.S3FileUtil;
import az.shopery.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserPhotoServiceImpl implements UserPhotoService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final S3FileUtil s3FileUtil;

    @Override
    @Transactional
    public SuccessResponse<String> uploadProfilePhoto(String userEmail, MultipartFile multipartFile) {
        String fileKey = fileStorageService.store(multipartFile);
        UserEntity userEntity = getUserByEmail(userEmail);
        userEntity.setProfilePhotoUrl(fileKey);
        userRepository.save(userEntity);
        log.info("Saved profile photo key for {}: {}", userEmail, fileKey);
        return SuccessResponse.of(s3FileUtil.generatePresignedUrl(fileKey), "Profile photo uploaded successfully. User key to get presigned URL.");
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
