package az.shopery.core_ms.service;

import az.shopery.core_ms.model.dto.request.ChatRequestDto;
import az.shopery.core_ms.model.dto.response.ChatResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;

public interface AiService {
    SuccessResponse<ChatResponseDto> chat(String userEmail, ChatRequestDto chatRequestDto);
}
