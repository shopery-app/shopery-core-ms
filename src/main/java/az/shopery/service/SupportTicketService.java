package az.shopery.service;

import az.shopery.model.dto.request.CreateSupportTicketRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;

public interface SupportTicketService {
    SuccessResponseDto<Void> createSupportTicket(CreateSupportTicketRequestDto createSupportTicketRequestDto, String userEmail);
}
