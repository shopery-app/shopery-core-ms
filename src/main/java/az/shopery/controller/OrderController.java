package az.shopery.controller;

import az.shopery.model.dto.response.OrderResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.OrderService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<SuccessResponseDto<List<OrderResponseDto>>> checkout(Principal principal) {
        return ResponseEntity.ok(orderService.checkoutFromCart(principal.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<SuccessResponseDto<List<OrderResponseDto>>> getMyOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getMyOrders(principal.getName()));
    }
}
