package az.shopery.service;

import az.shopery.model.dto.response.OrderResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import java.util.List;

public interface OrderService {
    SuccessResponse<List<OrderResponseDto>> checkoutFromCart(String userEmail);
    SuccessResponse<List<OrderResponseDto>> getMyOrders(String userEmail);
}
