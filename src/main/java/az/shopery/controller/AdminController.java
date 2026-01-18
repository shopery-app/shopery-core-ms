package az.shopery.controller;

import az.shopery.model.dto.request.CloseMerchantRequestDto;
import az.shopery.model.dto.request.ShopCreationRequestRejectDto;
import az.shopery.model.dto.response.ShopCreationRequestResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.SupportTicketResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import az.shopery.service.AdminService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/customers")
    public ResponseEntity<SuccessResponseDto<Page<UserProfileResponseDto>>> getCustomers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getCustomers(pageable));
    }

    @GetMapping("/merchants")
    public ResponseEntity<SuccessResponseDto<Page<UserProfileResponseDto>>> getMerchants(Pageable pageable) {
        return ResponseEntity.ok(adminService.getMerchants(pageable));
    }

    @PatchMapping("/users/close")
    public ResponseEntity<SuccessResponseDto<Void>> closeUser(@RequestBody @Valid CloseMerchantRequestDto closeMerchantRequestDto) {
        return ResponseEntity.ok(adminService.closeMerchant(closeMerchantRequestDto));
    }

    @GetMapping("/support-tickets")
    public ResponseEntity<SuccessResponseDto<Page<SupportTicketResponseDto>>> getSupportTickets(Pageable pageable, Principal principal) {
        return ResponseEntity.ok(adminService.getSupportTickets(pageable, principal.getName()));
    }

    @PatchMapping("/support-tickets/{id}/close")
    public ResponseEntity<SuccessResponseDto<Void>> closeSupportTickets(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(adminService.closeSupportTicket(id, principal.getName()));
    }

    @GetMapping("/shop-creation-requests")
    public ResponseEntity<SuccessResponseDto<Page<ShopCreationRequestResponseDto>>> getShopCreationRequests(Pageable pageable, Principal principal) {
        return ResponseEntity.ok(adminService.getShopCreationRequestsByAssignedAdmin(principal.getName(), pageable));
    }

    @PostMapping("/shop-creation-requests/{id}/approve")
    public ResponseEntity<SuccessResponseDto<Void>> approve(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(adminService.approve(id, principal.getName()));
    }

    @PostMapping("/shop-creation-requests/{id}/reject")
    public ResponseEntity<SuccessResponseDto<Void>> reject(@PathVariable String id, @Valid @RequestBody ShopCreationRequestRejectDto shopCreationRequestRejectDto, Principal principal) {
        return ResponseEntity.ok(adminService.reject(id, principal.getName(), shopCreationRequestRejectDto));
    }
}
