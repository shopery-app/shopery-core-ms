package az.shopery.controller;

import az.shopery.model.dto.request.ChatRequestDto;
import az.shopery.model.dto.response.ChatResponseDto;
import az.shopery.model.dto.response.SuccessResponseDto;
import az.shopery.service.ClaudeService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyAuthority('MERCHANT')")
public class ChatController {

    private final ClaudeService claudeService;

    @PostMapping
    public ResponseEntity<SuccessResponseDto<ChatResponseDto>> chat(Principal principal, @Valid @RequestBody ChatRequestDto chatRequestDto) {
        return ResponseEntity.ok(claudeService.chat(principal.getName(), chatRequestDto));
    }
}
