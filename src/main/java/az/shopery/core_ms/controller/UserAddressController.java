package az.shopery.core_ms.controller;

import az.shopery.core_ms.client.UserClient;
import az.shopery.core_ms.model.dto.request.AddressRequestDto;
import az.shopery.core_ms.model.dto.response.AddressResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
public class UserAddressController {

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<AddressResponseDto>>> getMyAddresses(Principal principal) {
        return userClient.getMyAddresses(principal.getName());
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<AddressResponseDto>> addMyAddress(Principal principal, @Valid @RequestBody AddressRequestDto addressRequestDto) {
        return userClient.addMyAddress(principal.getName(), addressRequestDto);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<SuccessResponse<AddressResponseDto>> updateMyAddress(Principal principal, @PathVariable String addressId, @Valid @RequestBody AddressRequestDto addressRequestDto) {
        return userClient.updateMyAddress(principal.getName(), addressId, addressRequestDto);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<SuccessResponse<Void>> removeMyAddress(Principal principal, @PathVariable String addressId) {
        return userClient.removeMyAddress(principal.getName(), addressId);
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<SuccessResponse<Void>> setMyDefaultAddress(Principal principal, @PathVariable String addressId) {
        return userClient.setMyDefaultAddress(principal.getName(), addressId);
    }
}
