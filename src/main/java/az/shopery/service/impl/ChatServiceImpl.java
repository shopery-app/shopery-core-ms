package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ChatSendRequestDto;
import az.shopery.model.dto.response.ChatMessageResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.ChatMessageEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.ChatMessageRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.ChatService;
import az.shopery.utils.enums.MessageStatus;
import az.shopery.utils.enums.UserStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public SuccessResponse<ChatMessageResponseDto> sendMessage(String senderEmail, ChatSendRequestDto request) {
        UserEntity sender = userRepository.findByEmailAndStatus(senderEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        UserEntity receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        ChatMessageEntity entity = chatMessageRepository.save(
                ChatMessageEntity.builder()
                        .senderId(sender.getId())
                        .receiverId(receiver.getId())
                        .content(request.getContent().trim())
                        .status(MessageStatus.SENT)
                        .build()
        );

        ChatMessageResponseDto response = map(entity);

        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                response
        );

        messagingTemplate.convertAndSendToUser(
                sender.getEmail(),
                "/queue/messages",
                response
        );

        return SuccessResponse.of(response, "message sent successfully!");
    }

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<List<ChatMessageResponseDto>> getConversation(String userEmail, UUID otherUserId) {
        UserEntity userEntity = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return SuccessResponse.of(chatMessageRepository.findConversation(userEntity.getId(), otherUserId).stream()
                .map(this::map)
                .toList(), "conversation retrieved successfully!");
    }

    private ChatMessageResponseDto map(ChatMessageEntity entity) {
        return ChatMessageResponseDto.builder()
                .id(entity.getId())
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .readAt(entity.getReadAt())
                .build();
    }
}
