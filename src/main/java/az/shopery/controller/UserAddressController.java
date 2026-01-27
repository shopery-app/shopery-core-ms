package az.shopery.controller;

import az.shopery.model.dto.request.AddressRequestDto;
import az.shopery.model.dto.response.AddressResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.UserAddressService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
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
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('CUSTOMER', 'MERCHANT')")
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<SuccessResponseDto<List<AddressResponseDto>>> getMyAddresses(Principal principal) {
        return ResponseEntity.ok(userAddressService.getAll(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<SuccessResponseDto<AddressResponseDto>> addMyAddress(Principal principal, @Valid @RequestBody AddressRequestDto addressRequestDto) {
        return ResponseEntity.ok(userAddressService.add(principal.getName(), addressRequestDto));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<SuccessResponseDto<AddressResponseDto>> updateMyAddress(Principal principal, @PathVariable String addressId, @Valid @RequestBody AddressRequestDto addressRequestDto) {
        return ResponseEntity.ok(userAddressService.update(principal.getName(), addressId, addressRequestDto));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<SuccessResponseDto<Void>> removeMyAddress(Principal principal, @PathVariable String addressId) {
        return ResponseEntity.ok(userAddressService.remove(principal.getName(), addressId));
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<SuccessResponseDto<Void>> setMyDefaultAddress(Principal principal, @PathVariable String addressId) {
        return ResponseEntity.ok(userAddressService.setDefault(principal.getName(), addressId));
    }
}
