package az.shopery.core_ms.service;

import az.shopery.core_ms.model.dto.request.AddressRequestDto;
import az.shopery.core_ms.model.dto.response.AddressResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import java.util.List;

public interface UserAddressService {
    SuccessResponse<AddressResponseDto> add(String userEmail, AddressRequestDto dto);
    SuccessResponse<AddressResponseDto> update(String userEmail, String addressId, AddressRequestDto dto);
    SuccessResponse<Void> remove(String userEmail, String addressId);
    SuccessResponse<Void> setDefault(String userEmail, String addressId);
    SuccessResponse<List<AddressResponseDto>> getAll(String userEmail);
}
