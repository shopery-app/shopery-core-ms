package az.shopery.core_ms.controller;

import az.shopery.core_ms.client.ChatClient;
import az.shopery.core_ms.model.dto.response.ChatMessageResponseDto;
import az.shopery.core_ms.model.dto.response.ConversationResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatClient chatClient;

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<SuccessResponse<List<ChatMessageResponseDto>>> getConversation(@PathVariable UUID otherUserId, Principal principal) {
        return chatClient.getConversation(otherUserId, principal.getName());
    }

    @GetMapping("/conversations")
    public ResponseEntity<SuccessResponse<List<ConversationResponseDto>>> getConversations(Principal principal) {
        return chatClient.getConversations(principal.getName());
    }
}
