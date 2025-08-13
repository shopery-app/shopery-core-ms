package az.shopery.service;

import az.shopery.model.dto.request.CustomerProfileUpdateRequestDto;
import az.shopery.model.dto.response.BecomeMerchantResponseDto;
import az.shopery.model.dto.response.CustomerProfileResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.model.entity.UserEntity;

public interface CustomerService {
    void createCustomerProfile(UserEntity userEntity);
    SuccessResponseDto<CustomerProfileResponseDto> getCustomerProfile(String userEmail);
    SuccessResponseDto<CustomerProfileResponseDto> updateCustomerProfile(String userEmail, CustomerProfileUpdateRequestDto customerProfileUpdateRequestDto);
    SuccessResponseDto<BecomeMerchantResponseDto> becomeMerchant(String userEmail);
}
