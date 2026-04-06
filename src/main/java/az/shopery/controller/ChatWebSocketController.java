package az.shopery.controller;

import az.shopery.model.dto.request.ChatSendRequestDto;
import az.shopery.model.dto.response.ChatMessageResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.ChatService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public ResponseEntity<SuccessResponse<ChatMessageResponseDto>> sendMessage(@Valid ChatSendRequestDto request, Principal principal) {
        return ResponseEntity.ok(chatService.sendMessage(principal.getName(), request));
    }
}
