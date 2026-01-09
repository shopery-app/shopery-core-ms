package az.shopery.controller;

import az.shopery.model.dto.request.SupportTicketRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserSupportTicketResponseDto;
import az.shopery.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users/me/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class UserSupportTicketController {

    private final SupportTicketService supportTicketService;

    @GetMapping
    public ResponseEntity<SuccessResponseDto<Page<UserSupportTicketResponseDto>>> getMySupportTicket(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(supportTicketService.getMySupportTickets(principal.getName(), pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponseDto<Void>> deleteMySupportTicket(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(supportTicketService.deleteMySupportTicket(id, principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponseDto<UserSupportTicketResponseDto>> updateMySupportTicket(
            @RequestBody @Valid SupportTicketRequestDto supportTicketRequestDto,
            @PathVariable String id,
            Principal principal) {
        return ResponseEntity.ok(supportTicketService.updateMySupportTicket(supportTicketRequestDto, id, principal.getName()));
    }
}
