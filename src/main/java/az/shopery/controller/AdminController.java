package az.shopery.controller;

import az.shopery.model.dto.request.CloseMerchantRequestDto;
import az.shopery.model.dto.request.ShopCreationRequestRejectDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.model.dto.response.task.TaskResponseDto;
import az.shopery.service.AdminService;
import az.shopery.utils.enums.TaskCategory;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/customers")
    public ResponseEntity<SuccessResponse<Page<UserProfileResponseDto>>> getCustomers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getCustomers(pageable));
    }

    @GetMapping("/merchants")
    public ResponseEntity<SuccessResponse<Page<UserProfileResponseDto>>> getMerchants(Pageable pageable) {
        return ResponseEntity.ok(adminService.getMerchants(pageable));
    }

    @PatchMapping("/users/close")
    public ResponseEntity<SuccessResponse<Void>> closeUser(@RequestBody @Valid CloseMerchantRequestDto closeMerchantRequestDto) {
        return ResponseEntity.ok(adminService.closeMerchant(closeMerchantRequestDto));
    }

    @GetMapping("/tasks")
    public ResponseEntity<SuccessResponse<Page<TaskResponseDto>>> getTasks(@RequestParam(required = false) TaskCategory taskCategory, Pageable pageable, Principal principal) {
        return ResponseEntity.ok(adminService.getTasks(taskCategory, pageable, principal.getName()));
    }

    @PatchMapping("/tasks/{id}/close")
    public ResponseEntity<SuccessResponse<Void>> closeSupportTicket(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(adminService.closeSupportTicket(id, principal.getName()));
    }

    @PostMapping("/tasks/{id}/approve")
    public ResponseEntity<SuccessResponse<Void>> approve(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(adminService.approve(id, principal.getName()));
    }

    @PostMapping("/tasks/{id}/reject")
    public ResponseEntity<SuccessResponse<Void>> reject(@PathVariable String id, @Valid @RequestBody ShopCreationRequestRejectDto shopCreationRequestRejectDto, Principal principal) {
        return ResponseEntity.ok(adminService.reject(id, principal.getName(), shopCreationRequestRejectDto));
    }

    @GetMapping("/application/info")
    public ResponseEntity<SuccessResponse<Map<String, Integer>>> getApplicationInfo(Principal principal) {
        return ResponseEntity.ok((adminService.getApplicationInfo(principal.getName())));
    }
}
