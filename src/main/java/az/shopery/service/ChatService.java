package az.shopery.service;

import az.shopery.model.dto.request.ChatSendRequestDto;
import az.shopery.model.dto.response.ChatMessageResponseDto;
import az.shopery.model.dto.response.ConversationResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import java.util.List;
import java.util.UUID;

public interface ChatService {
    void sendMessage(String userEmail, ChatSendRequestDto chatSendRequestDto);
    SuccessResponse<List<ChatMessageResponseDto>> getConversation(String userEmail, UUID otherUserId);
    SuccessResponse<List<ConversationResponseDto>> getConversations(String userEmail);
}
