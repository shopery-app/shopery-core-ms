package az.shopery.service;

import az.shopery.model.dto.request.AddressRequestDto;
import az.shopery.model.dto.response.AddressResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import java.util.List;
import java.util.UUID;

public interface CustomerAddressService {
    SuccessResponseDto<AddressResponseDto> addAddress(String userEmail, AddressRequestDto addressRequestDto);
    SuccessResponseDto<AddressResponseDto> updateAddress(String userEmail, UUID addressId, AddressRequestDto addressRequestDto);
    SuccessResponseDto<Void> removeAddress(String userEmail, UUID addressId);
    SuccessResponseDto<Void> setDefaultAddress(String userEmail, UUID addressId);
    SuccessResponseDto<List<AddressResponseDto>> getAllAddresses(String userEmail);
}
