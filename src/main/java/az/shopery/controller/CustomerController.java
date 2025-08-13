package az.shopery.controller;

import az.shopery.model.dto.request.CustomerProfileUpdateRequestDto;
import az.shopery.model.dto.response.CustomerProfileResponseDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.CustomerService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers/me/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CUSTOMER')")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<SuccessResponseDto<CustomerProfileResponseDto>> getMyProfile(
            Principal principal) {
        return ResponseEntity.ok(customerService.getCustomerProfile(principal.getName()));
    }

    @PutMapping
    public ResponseEntity<SuccessResponseDto<CustomerProfileResponseDto>> updateMyProfile(
            Principal principal, @Valid @RequestBody CustomerProfileUpdateRequestDto customerProfileUpdateRequestDto) {
        return ResponseEntity.ok(customerService.updateCustomerProfile(principal.getName(), customerProfileUpdateRequestDto));
    }

    @PostMapping("/become-merchant")
    public ResponseEntity<SuccessResponseDto<BecomeMerchantResponseDto>> becomeMerchant(
            Principal principal) {
        return ResponseEntity.ok(customerService.becomeMerchant(principal.getName()));
    }
}
