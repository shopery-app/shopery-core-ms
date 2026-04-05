package az.shopery.controller;

import az.shopery.model.dto.response.OrderResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.OrderService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<SuccessResponse<List<OrderResponseDto>>> checkout(Principal principal) {
        return ResponseEntity.ok(orderService.checkoutFromCart(principal.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<List<OrderResponseDto>>> getMyOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getMyOrders(principal.getName()));
    }
}
