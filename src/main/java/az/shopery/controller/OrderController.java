package az.shopery.controller;

import az.shopery.client.OrderClient;
import az.shopery.model.dto.response.OrderResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/orders")
public class OrderController {

    private final OrderClient orderClient;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<OrderResponseDto>>> getMyOrders(Principal principal) {
        return orderClient.getMyOrders(principal.getName());
    }
}
