package az.shopery.controller;

import az.shopery.model.dto.request.ChatRequestDto;
import az.shopery.model.dto.response.ChatResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.ClaudeService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ClaudeService claudeService;

    @PostMapping
    public ResponseEntity<SuccessResponse<ChatResponseDto>> chat(Principal principal, @Valid @RequestBody ChatRequestDto chatRequestDto) {
        return ResponseEntity.ok(claudeService.chat(principal.getName(), chatRequestDto));
    }
}
