package az.shopery.core_ms.controller;

import az.shopery.core_ms.model.dto.request.ChatRequestDto;
import az.shopery.core_ms.model.dto.response.ChatResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.service.AiService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/chat")
public class AiController {

    private final AiService aiService;

    @PostMapping
    public ResponseEntity<SuccessResponse<ChatResponseDto>> chat(Principal principal, @Valid @RequestBody ChatRequestDto chatRequestDto) {
        return ResponseEntity.ok(aiService.chat(principal.getName(), chatRequestDto));
    }
}
