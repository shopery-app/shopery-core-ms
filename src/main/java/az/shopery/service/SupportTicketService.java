package az.shopery.service;

import az.shopery.model.dto.request.SupportTicketRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.UserSupportTicketResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportTicketService {
    SuccessResponseDto<Void> createSupportTicket(SupportTicketRequestDto supportTicketRequestDto, String userEmail);
    SuccessResponseDto<Page<UserSupportTicketResponseDto>> getMySupportTickets(String userEmail, Pageable pageable);
    SuccessResponseDto<Void> deleteMySupportTicket(String id, String userEmail);
    SuccessResponseDto<UserSupportTicketResponseDto> updateMySupportTicket(SupportTicketRequestDto updateSupportTicketRequestDto, String id, String userEmail);
}
