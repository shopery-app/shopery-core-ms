package az.shopery.service;

import az.shopery.model.dto.request.CloseMerchantRequestDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.dto.response.SupportTicketResponseDto;
import az.shopery.model.dto.response.UserProfileResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface AdminService {
    SuccessResponseDto<Page<UserProfileResponseDto>> getCustomers(Pageable pageable);
    SuccessResponseDto<Page<UserProfileResponseDto>> getMerchants(Pageable pageable);
    SuccessResponseDto<Void> closeMerchant(CloseMerchantRequestDto closeMerchantRequestDto);
    SuccessResponseDto<Page<SupportTicketResponseDto>> getSupportTickets(Pageable pageable, String userEmail);
    SuccessResponseDto<Void> closeSupportTicket(String id, String userEmail);
}
