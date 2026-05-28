package az.shopery.controller;

import az.shopery.client.BackofficeClient;
import az.shopery.model.dto.request.ShopCreationRequestRejectDto;
import az.shopery.model.dto.response.AdminShopResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.dto.response.task.TaskResponseDto;
import az.shopery.utils.enums.TaskCategory;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
public class AdminController {

    private final BackofficeClient backofficeClient;

    @GetMapping("/users")
    public ResponseEntity<SuccessResponse<Page<UserProfileResponseDto>>> getUsers(Pageable pageable) {
        return backofficeClient.getUsers(pageable);
    }

    @PatchMapping("/users/{id}/close")
    public ResponseEntity<SuccessResponse<Void>> closeUser(@PathVariable String id) {
        return backofficeClient.closeUser(id);
    }

    @GetMapping("/shops")
    public ResponseEntity<SuccessResponse<Page<AdminShopResponseDto>>> getShops(Pageable pageable) {
        return backofficeClient.getShops(pageable);
    }

    @GetMapping("/tasks")
    public ResponseEntity<SuccessResponse<Page<TaskResponseDto>>> getTasks(@RequestParam(required = false) TaskCategory taskCategory, Pageable pageable, Principal principal) {
        return backofficeClient.getTasks(taskCategory, pageable, principal.getName());
    }

    @PatchMapping("/tasks/{id}/close")
    public ResponseEntity<SuccessResponse<Void>> closeSupportTicket(@PathVariable String id, Principal principal) {
        return backofficeClient.closeSupportTicket(id, principal.getName());
    }

    @PostMapping("/tasks/{id}/approve")
    public ResponseEntity<SuccessResponse<Void>> approve(@PathVariable String id, Principal principal) {
        return backofficeClient.approve(id, principal.getName());
    }

    @PostMapping("/tasks/{id}/reject")
    public ResponseEntity<SuccessResponse<Void>> reject(@PathVariable String id, @Valid @RequestBody ShopCreationRequestRejectDto shopCreationRequestRejectDto, Principal principal) {
        return backofficeClient.reject(id, shopCreationRequestRejectDto, principal.getName());
    }
}
