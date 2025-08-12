package az.shopery.service;

import az.shopery.model.dto.request.AddressRequestDto;
import az.shopery.model.dto.response.AddressResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import java.util.List;

public interface CustomerAddressService {
    SuccessResponseDto<AddressResponseDto> addAddress(String userEmail, AddressRequestDto addressRequestDto);
    SuccessResponseDto<AddressResponseDto> updateAddress(String userEmail, String addressId, AddressRequestDto addressRequestDto);
    SuccessResponseDto<Void> removeAddress(String userEmail, String addressId);
    SuccessResponseDto<Void> setDefaultAddress(String userEmail, String addressId);
    SuccessResponseDto<List<AddressResponseDto>> getAllAddresses(String userEmail);
}
