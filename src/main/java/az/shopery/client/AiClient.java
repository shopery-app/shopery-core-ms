package az.shopery.client;

import az.shopery.model.dto.request.ChatRequestClientDto;
import az.shopery.model.dto.response.ChatResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-ms", url = "${feign.client.config.ai-ms.url}")
public interface AiClient {

    @PostMapping("/api/v1/ai/chat")
    ResponseEntity<SuccessResponse<ChatResponseDto>> chat(@RequestBody ChatRequestClientDto chatRequestClientDto);
}
