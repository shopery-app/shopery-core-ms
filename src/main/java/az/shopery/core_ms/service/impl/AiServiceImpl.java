package az.shopery.core_ms.service.impl;

import static az.shopery.core_ms.utils.common.CommonConstraints.PREMIUM_MAX_TOKENS;

import az.shopery.core_ms.client.AiClient;
import az.shopery.core_ms.handler.exception.ApplicationException;
import az.shopery.core_ms.handler.exception.ExternalServiceException;
import az.shopery.core_ms.handler.exception.ResourceNotFoundException;
import az.shopery.core_ms.model.dto.client.ChatRequestClientDto;
import az.shopery.core_ms.model.dto.request.ChatRequestDto;
import az.shopery.core_ms.model.dto.response.ChatResponseDto;
import az.shopery.core_ms.model.dto.shared.SuccessResponse;
import az.shopery.core_ms.model.entity.UserEntity;
import az.shopery.core_ms.repository.UserRepository;
import az.shopery.core_ms.service.AiService;
import az.shopery.core_ms.utils.enums.SubscriptionTier;
import az.shopery.core_ms.utils.enums.UserRole;
import az.shopery.core_ms.utils.enums.UserStatus;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiClient aiClient;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SuccessResponse<ChatResponseDto> chat(String userEmail, ChatRequestDto request) {
        log.info("Processing chat request from user: {}", userEmail);

        UserEntity user = userRepository.findByEmailAndUserRoleAndStatusAndSubscriptionTier(userEmail, UserRole.USER, UserStatus.ACTIVE, SubscriptionTier.PREMIUM)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        long currentUsage = user.getMonthlyAiTokensUsed();
        if (currentUsage >= PREMIUM_MAX_TOKENS) {
            throw new ApplicationException("Monthly AI token limit exceeded!");
        }

        long remainingTokens = PREMIUM_MAX_TOKENS - currentUsage;
        var response = aiClient.chat(ChatRequestClientDto.builder()
                .message(request.getMessage())
                .remainingTokens((int) remainingTokens)
                .build());
        var responseBody = response.getBody();

        if (Objects.isNull(responseBody) || Objects.isNull(responseBody.getData())) {
            throw new ExternalServiceException("Invalid response from AI service!");
        }

        ChatResponseDto chatResponseDto = responseBody.getData();

        user.setMonthlyAiTokensUsed(currentUsage + chatResponseDto.getTokensUsed());
        userRepository.save(user);

        log.info("User: {} used {} tokens. Total usage: {}", userEmail, chatResponseDto.getTokensUsed(), user.getMonthlyAiTokensUsed());

        return responseBody;
    }
}
