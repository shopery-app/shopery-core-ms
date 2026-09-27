package az.shopery.core_ms.service;

import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserPhotoService {
    SuccessResponse<byte[]> uploadProfilePhoto(String userEmail, MultipartFile multipartFile);
    SuccessResponse<Void> deleteProfilePhoto(String userEmail);
}
