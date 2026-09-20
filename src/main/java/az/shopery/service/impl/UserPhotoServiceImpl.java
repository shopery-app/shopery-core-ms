package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserRepository;
import az.shopery.service.UserPhotoService;
import az.shopery.utils.common.FilenetClientHelper;
import az.shopery.utils.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPhotoServiceImpl implements UserPhotoService {

    private final UserRepository userRepository;
    private final FilenetClientHelper filenetClientHelper;

    @Override
    @Transactional
    public SuccessResponse<byte[]> uploadProfilePhoto(String userEmail, MultipartFile multipartFile) {
        UserEntity userEntity = getUserByEmail(userEmail);
        userEntity.setProfilePhotoId(filenetClientHelper.saveFile(multipartFile));
        userRepository.save(userEntity);

        log.info("Saved profile photo for: {}", userEmail);
        byte[] content = filenetClientHelper.getFile(userEntity.getProfilePhotoId());
        return SuccessResponse.of(content, "Profile photo uploaded successfully!");
    }

    @Override
    @Transactional
    public SuccessResponse<Void> deleteProfilePhoto(String userEmail) {
        UserEntity userEntity = getUserByEmail(userEmail);

        filenetClientHelper.deleteFile(userEntity.getProfilePhotoId());
        userEntity.setProfilePhotoId(null);
        userRepository.save(userEntity);

        log.info("Deleted profile photo for: {}", userEmail);
        return SuccessResponse.of(null, "Profile photo deleted successfully!");
    }

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
