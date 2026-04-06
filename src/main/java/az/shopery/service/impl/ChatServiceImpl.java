package az.shopery.service.impl;

import az.shopery.handler.exception.ResourceNotFoundException;
import az.shopery.model.dto.request.ChatSendRequestDto;
import az.shopery.model.dto.response.ChatMessageResponseDto;
import az.shopery.model.dto.response.ConversationResponseDto;
import az.shopery.model.dto.shared.SuccessResponse;
import az.shopery.model.entity.ChatMessageEntity;
import az.shopery.model.entity.UserEntity;
import az.shopery.repository.ChatMessageRepository;
import az.shopery.repository.UserRepository;
import az.shopery.service.ChatService;
import az.shopery.utils.enums.MessageStatus;
import az.shopery.utils.enums.UserStatus;
import java.util.List;
import java.util.Objects;
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
    public void sendMessage(String senderEmail, ChatSendRequestDto request) {
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

    @Override
    @Transactional(readOnly = true)
    public SuccessResponse<List<ConversationResponseDto>> getConversations(String userEmail) {
        UserEntity me = userRepository.findByEmailAndStatus(userEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<UUID> contactIds = chatMessageRepository.findDistinctContactsByUserId(me.getId());

        List<ConversationResponseDto> conversations = contactIds.stream()
                .map(contactId -> {
                    UserEntity contact = userRepository.findById(contactId).orElse(null);
                    if (Objects.isNull(contact)) return null;

                    ChatMessageEntity lastMsg = chatMessageRepository
                            .findTopBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtDesc(
                                    me.getId(), contactId, contactId, me.getId())
                            .orElse(null);

                    return ConversationResponseDto.builder()
                            .buyerId(contactId)
                            .buyerName(contact.getName())
                            .lastMessage(Objects.nonNull(lastMsg) ? map(lastMsg) : null)
                            .build();
                }).filter(Objects::nonNull).toList();

        return SuccessResponse.of(conversations, "conversations retrieved successfully!");
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
