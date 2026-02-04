package az.shopery.controller;

import az.shopery.model.dto.request.SupportTicketRequestDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserSupportTicketResponseDto;
import az.shopery.service.SupportTicketService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class UserSupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    public ResponseEntity<SuccessResponse<Void>> createMySupportTicket(@Valid @RequestBody SupportTicketRequestDto supportTicketRequestDto, Principal principal) {
        return ResponseEntity.ok(supportTicketService.createMySupportTicket(supportTicketRequestDto, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<UserSupportTicketResponseDto>>> getMySupportTicket(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(supportTicketService.getMySupportTickets(principal.getName(), pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteMySupportTicket(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(supportTicketService.deleteMySupportTicket(id, principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserSupportTicketResponseDto>> updateMySupportTicket(@Valid @RequestBody SupportTicketRequestDto supportTicketRequestDto, @PathVariable String id, Principal principal) {
        return ResponseEntity.ok(supportTicketService.updateMySupportTicket(supportTicketRequestDto, id, principal.getName()));
    }
}
