package az.shopery.controller;

import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.UserPhotoService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/me/photo")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class UserPhotoController {

    private final UserPhotoService userPhotoService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<SuccessResponseDto<String>> uploadMyProfilePhoto(Principal principal, @RequestParam("file") MultipartFile multipartFile) {
        return ResponseEntity.ok(userPhotoService.uploadProfilePhoto(principal.getName(), multipartFile));
    }

    @GetMapping("/url")
    public ResponseEntity<SuccessResponseDto<String>> getMyProfilePhotoUrl(Principal principal) {
        return ResponseEntity.ok(userPhotoService.generatePresignedUrlForPhoto(principal.getName()));
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponseDto<Void>> deleteMyProfilePhoto(Principal principal) {
        return ResponseEntity.ok(userPhotoService.deleteProfilePhoto(principal.getName()));
    }
}
