package az.shopery.controller;

import az.shopery.model.dto.request.AddressRequestDto;
import az.shopery.model.dto.response.AddressResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.CustomerAddressService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers/me/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CUSTOMER')")
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;

    @GetMapping
    public ResponseEntity<SuccessResponseDto<List<AddressResponseDto>>> getMyAddresses(
            Principal principal) {
        return ResponseEntity.ok(customerAddressService.getAllAddresses(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<SuccessResponseDto<AddressResponseDto>> addMyAddress(
            Principal principal, @Valid @RequestBody AddressRequestDto addressRequestDto) {
        return ResponseEntity.ok(customerAddressService.addAddress(principal.getName(), addressRequestDto));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<SuccessResponseDto<AddressResponseDto>> updateMyAddress(
            Principal principal, @PathVariable UUID addressId, @Valid @RequestBody AddressRequestDto addressRequestDto) {
        return ResponseEntity.ok(customerAddressService.updateAddress(principal.getName(), addressId, addressRequestDto));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<SuccessResponseDto<Void>> deleteMyAddress(
            Principal principal, @PathVariable UUID addressId) {
        return ResponseEntity.ok(customerAddressService.removeAddress(principal.getName(), addressId));
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<SuccessResponseDto<Void>> setMyDefaultAddress(
            Principal principal, @PathVariable UUID addressId) {
        return ResponseEntity.ok(customerAddressService.setDefaultAddress(principal.getName(), addressId));
    }
}
