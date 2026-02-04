package az.shopery.service.impl;

import az.shopery.configuration.ClaudeApiConfig;
import az.shopery.handler.exception.ExternalServiceException;
import az.shopery.model.dto.request.ChatRequestDto;
import az.shopery.model.dto.request.ClaudeRequestDto;
import az.shopery.model.dto.response.ChatResponseDto;
import az.shopery.model.dto.response.ClaudeResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.service.ClaudeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ClaudeServiceImpl implements ClaudeService {

    private final ClaudeApiConfig config;
    private final WebClient webClient;

    public ClaudeServiceImpl(ClaudeApiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", config.getKey())
                .defaultHeader("anthropic-version", config.getVersion())
                .build();
    }

    @Override
    public SuccessResponse<ChatResponseDto> chat(String userEmail, ChatRequestDto request) {
        log.info("Processing chat request from user: {}", userEmail);

        ClaudeRequestDto claudeRequest = ClaudeRequestDto.builder()
                .model(config.getModel())
                .maxTokens(config.getMaxTokens())
                .messages(List.of(
                        ClaudeRequestDto.Message.builder()
                                .role("user")
                                .content(request.getMessage())
                                .build()
                ))
                .build();

        return SuccessResponse.of(callClaudeApi(claudeRequest), "Success");
    }

    private ChatResponseDto callClaudeApi(ClaudeRequestDto request) {
        try {
            ClaudeResponseDto response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClaudeResponseDto.class)
                    .onErrorResume(error -> {
                        log.error("Error calling Claude API", error);
                        return Mono.error(new ExternalServiceException("Failed to communicate with Claude API: " + error.getMessage()));
                    })
                    .block();

            if (Objects.isNull(response) || Objects.isNull(response.getContent()) || response.getContent().isEmpty()) {
                throw new ExternalServiceException("Empty response from Claude API");
            }

            String messageContent = response.getContent().getFirst().getText();
            Integer totalTokens = response.getUsage().getInputTokens() + response.getUsage().getOutputTokens();

            log.info("Claude API response received. Tokens used: {}", totalTokens);

            return ChatResponseDto.builder()
                    .message(messageContent)
                    .tokensUsed(totalTokens)
                    .build();

        } catch (Exception e) {
            log.error("Unexpected error during Claude API call", e);
            throw new ExternalServiceException("An error occurred while processing your request");
        }
    }
}
