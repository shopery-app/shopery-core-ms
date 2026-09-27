package az.shopery.core_ms.service;

import az.shopery.core_ms.model.dto.request.ChatSendRequestDto;
import az.shopery.core_ms.model.dto.response.ChatMessageResponseDto;
import az.shopery.core_ms.model.dto.response.ConversationResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import java.util.List;
import java.util.UUID;

public interface ChatService {
    void sendMessage(String userEmail, ChatSendRequestDto chatSendRequestDto);
    SuccessResponse<List<ChatMessageResponseDto>> getConversation(String userEmail, UUID otherUserId);
    SuccessResponse<List<ConversationResponseDto>> getConversations(String userEmail);
}
