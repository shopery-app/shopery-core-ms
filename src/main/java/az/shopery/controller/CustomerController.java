package az.shopery.controller;

import az.shopery.model.dto.response.CustomerProfileResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.CustomerService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers/me")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CUSTOMER')")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/profile")
    public ResponseEntity<SuccessResponseDto<CustomerProfileResponseDto>> getMyProfile(
            Principal principal) {
        return ResponseEntity.ok(customerService.getCustomerProfile(principal.getName()));
    }
}
