package az.shopery.service;

import az.shopery.model.dto.response.UserAuthResponseDto;
import az.shopery.model.dto.request.UserLoginRequestDto;
import az.shopery.model.dto.request.UserRegisterRequestDto;

public interface AuthService {
    UserAuthResponseDto register(UserRegisterRequestDto userRegisterRequestDto);
    UserAuthResponseDto login(UserLoginRequestDto userLoginRequestDto);
}
