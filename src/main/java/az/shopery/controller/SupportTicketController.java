package az.shopery.controller;

import az.shopery.model.dto.request.CreateSupportTicketRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    public ResponseEntity<SuccessResponseDto<Void>> createSupportTicket(
            @RequestBody @Valid CreateSupportTicketRequestDto createSupportTicketRequestDto,
            Principal principal) {
        return ResponseEntity.ok(supportTicketService.createSupportTicket(createSupportTicketRequestDto, principal.getName()));
    }
}
