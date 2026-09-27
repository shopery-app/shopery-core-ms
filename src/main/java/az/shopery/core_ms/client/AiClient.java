package az.shopery.core_ms.client;

import az.shopery.core_ms.model.dto.client.ChatRequestClientDto;
import az.shopery.core_ms.model.dto.response.ChatResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-ms", url = "${feign.client.config.ai-ms.url}")
public interface AiClient {

    @PostMapping("/api/v1/ai/chat")
    ResponseEntity<SuccessResponse<ChatResponseDto>> chat(@RequestBody ChatRequestClientDto chatRequestClientDto);
}
