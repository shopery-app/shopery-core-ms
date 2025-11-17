package az.shopery.service;

import az.shopery.model.dto.response.OrderResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import java.util.List;

public interface OrderService {
    SuccessResponseDto<List<OrderResponseDto>> checkoutFromCart(String userEmail);
    SuccessResponseDto<List<OrderResponseDto>> getMyOrders(String userEmail);
}
