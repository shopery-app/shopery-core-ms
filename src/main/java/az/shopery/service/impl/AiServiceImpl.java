package az.shopery.service.impl;

import static az.shopery.utils.common.CommonConstraints.PREMIUM_MAX_TOKENS;

import az.shopery.client.AiClient;
import az.shopery.handler.exception.ApplicationException;
import az.shopery.handler.exception.ExternalServiceException;
import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ChatRequestClientDto;
import az.shopery.model.dto.request.ChatRequestDto;
import az.shopery.model.dto.response.ChatResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.UserRepository;
import az.shopery.service.AiService;
import az.shopery.utils.enums.SubscriptionTier;
import az.shopery.utils.enums.UserRole;
import az.shopery.utils.enums.UserStatus;
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

        var response = aiClient.chat(ChatRequestClientDto.builder()
                .message(request.getMessage())
                .remainingTokens(PREMIUM_MAX_TOKENS - currentUsage)
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
