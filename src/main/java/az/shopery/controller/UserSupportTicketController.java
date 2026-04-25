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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/support-tickets")
@RequiredArgsConstructor
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
