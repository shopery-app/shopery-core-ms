package az.shopery.core_ms.client;

import az.shopery.core_ms.model.dto.response.ChatMessageResponseDto;
import az.shopery.core_ms.model.dto.response.ConversationResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "chat-ms", url = "${feign.client.config.chat-ms.url}")
public interface ChatClient {

    @GetMapping("/api/v1/chat/conversation/{otherUserId}")
    ResponseEntity<SuccessResponse<List<ChatMessageResponseDto>>> getConversation(@PathVariable UUID otherUserId, @RequestParam String email);

    @GetMapping("/api/v1/chat/conversations")
    ResponseEntity<SuccessResponse<List<ConversationResponseDto>>> getConversations(@RequestParam String email);
}
