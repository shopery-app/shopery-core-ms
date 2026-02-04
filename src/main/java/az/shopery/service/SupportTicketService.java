package az.shopery.service;

import az.shopery.model.dto.request.SupportTicketRequestDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.dto.response.UserSupportTicketResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportTicketService {
    SuccessResponse<Void> createMySupportTicket(SupportTicketRequestDto supportTicketRequestDto, String userEmail);
    SuccessResponse<Page<UserSupportTicketResponseDto>> getMySupportTickets(String userEmail, Pageable pageable);
    SuccessResponse<Void> deleteMySupportTicket(String id, String userEmail);
    SuccessResponse<UserSupportTicketResponseDto> updateMySupportTicket(SupportTicketRequestDto updateSupportTicketRequestDto, String id, String userEmail);
}
