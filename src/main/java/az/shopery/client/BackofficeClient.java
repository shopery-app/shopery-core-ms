package az.shopery.client;

import az.shopery.model.dto.request.ShopCreationRequestRejectDto;
import az.shopery.model.dto.response.AdminShopResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.dto.response.task.TaskResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.utils.enums.TaskCategory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "backoffice-ms", url = "${feign.client.config.backoffice-ms.url}")
public interface BackofficeClient {

    @GetMapping("/api/v1/admins/users")
    ResponseEntity<SuccessResponse<Page<UserProfileResponseDto>>> getUsers(Pageable pageable);

    @PatchMapping("/api/v1/admins/users/{id}/close")
    ResponseEntity<SuccessResponse<Void>> closeUser(@PathVariable String id);

    @GetMapping("/api/v1/admins/shops")
    ResponseEntity<SuccessResponse<Page<AdminShopResponseDto>>> getShops(Pageable pageable);

    @GetMapping("/api/v1/admins/tasks")
    ResponseEntity<SuccessResponse<Page<TaskResponseDto>>> getTasks(@RequestParam(required = false) TaskCategory taskCategory, Pageable pageable, @RequestParam String email);

    @PatchMapping("/api/v1/admins/tasks/{id}/close")
    ResponseEntity<SuccessResponse<Void>> closeSupportTicket(@PathVariable String id, @RequestParam String email);

    @PostMapping("/api/v1/admins/tasks/{id}/approve")
    ResponseEntity<SuccessResponse<Void>> approve(@PathVariable String id, @RequestParam String email);

    @PostMapping("/api/v1/admins/tasks/{id}/reject")
    ResponseEntity<SuccessResponse<Void>> reject(@PathVariable String id, @RequestBody ShopCreationRequestRejectDto shopCreationRequestRejectDto, @RequestParam String email);
}
