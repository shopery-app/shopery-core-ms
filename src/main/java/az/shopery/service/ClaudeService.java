package az.shopery.service;

import az.shopery.model.dto.request.ChatRequestDto;
import az.shopery.model.dto.response.ChatResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;

public interface ClaudeService {
    SuccessResponse<ChatResponseDto> chat(String userEmail, ChatRequestDto chatRequestDto);
}
