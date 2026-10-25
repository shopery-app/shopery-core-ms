package az.shopery.core_ms.client;

import az.shopery.core_ms.model.dto.request.AddressRequestDto;
import az.shopery.core_ms.model.dto.request.SupportTicketRequestDto;
import az.shopery.core_ms.model.dto.request.UserProfileUpdateRequestDto;
import az.shopery.core_ms.model.dto.response.AddressResponseDto;
import az.shopery.core_ms.model.dto.response.UserProfileResponseDto;
import az.shopery.core_ms.model.dto.response.UserSupportTicketResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "user-ms", url = "${feign.client.config.user-ms.url}")
public interface UserClient {

    @GetMapping("/api/v1/users/me/profile")
    ResponseEntity<SuccessResponse<UserProfileResponseDto>> getMyProfile(@RequestParam String email);

    @PutMapping("/api/v1/users/me/profile")
    ResponseEntity<SuccessResponse<UserProfileResponseDto>> updateMyProfile(@RequestParam String email, @RequestBody UserProfileUpdateRequestDto userProfileUpdateRequestDto);

    @PostMapping(value = "/api/v1/users/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<SuccessResponse<byte[]>> uploadMyProfilePhoto(@RequestParam String email, @RequestPart("file") MultipartFile multipartFile);

    @DeleteMapping("/api/v1/users/me/photo")
    ResponseEntity<SuccessResponse<Void>> deleteMyProfilePhoto(@RequestParam String email);

    @GetMapping("/api/v1/users/me/addresses")
    ResponseEntity<SuccessResponse<List<AddressResponseDto>>> getMyAddresses(@RequestParam String email);

    @PostMapping("/api/v1/users/me/addresses")
    ResponseEntity<SuccessResponse<AddressResponseDto>> addMyAddress(@RequestParam String email, @RequestBody AddressRequestDto addressRequestDto);

    @PutMapping("/api/v1/users/me/addresses/{addressId}")
    ResponseEntity<SuccessResponse<AddressResponseDto>> updateMyAddress(@RequestParam String email, @PathVariable String addressId, @RequestBody AddressRequestDto addressRequestDto);

    @DeleteMapping("/api/v1/users/me/addresses/{addressId}")
    ResponseEntity<SuccessResponse<Void>> removeMyAddress(@RequestParam String email, @PathVariable String addressId);

    @PutMapping("/api/v1/users/me/addresses/{addressId}/default")
    ResponseEntity<SuccessResponse<Void>> setMyDefaultAddress(@RequestParam String email, @PathVariable String addressId);

    @PostMapping("/api/v1/users/me/support-tickets")
    ResponseEntity<SuccessResponse<Void>> createMySupportTicket(@RequestParam String email, @RequestBody SupportTicketRequestDto supportTicketRequestDto);

    @GetMapping("/api/v1/users/me/support-tickets")
    ResponseEntity<SuccessResponse<Page<UserSupportTicketResponseDto>>> getMySupportTickets(@RequestParam String email, Pageable pageable);

    @DeleteMapping("/api/v1/users/me/support-tickets/{id}")
    ResponseEntity<SuccessResponse<Void>> deleteMySupportTicket(@RequestParam String email, @PathVariable String id);

    @PutMapping("/api/v1/users/me/support-tickets/{id}")
    ResponseEntity<SuccessResponse<UserSupportTicketResponseDto>> updateMySupportTicket(@RequestParam String email, @PathVariable String id, @RequestBody SupportTicketRequestDto supportTicketRequestDto);
}
