package az.shopery.controller;

import az.shopery.model.dto.request.ChatSendRequestDto;
import az.shopery.service.ChatService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid ChatSendRequestDto request, Principal principal) {
        chatService.sendMessage(principal.getName(), request);
    }
}
