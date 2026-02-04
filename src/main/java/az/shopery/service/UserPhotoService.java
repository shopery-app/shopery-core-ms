package az.shopery.service;

import az.shopery.model.dto.shared.SuccessResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserPhotoService {
    SuccessResponse<String> uploadProfilePhoto(String userEmail, MultipartFile multipartFile);
    SuccessResponse<Void> deleteProfilePhoto(String userEmail);
}
