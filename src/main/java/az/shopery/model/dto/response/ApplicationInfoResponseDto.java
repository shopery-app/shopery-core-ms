package az.shopery.model.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ApplicationInfoResponseDto {
    Integer totalCustomers;
    Integer totalMerchants;
    Integer pendingSupportTickets;
    Integer pendingShopCreationRequests;
}
