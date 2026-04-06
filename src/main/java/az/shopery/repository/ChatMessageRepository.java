package az.shopery.repository;

import az.shopery.model.entity.ChatMessageEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {
    @Query("""
        SELECT cm
        FROM ChatMessageEntity cm
        WHERE (cm.senderId = :user1 AND cm.receiverId = :user2)
           OR (cm.senderId = :user2 AND cm.receiverId = :user1)
        ORDER BY cm.createdAt ASC
    """)
    List<ChatMessageEntity> findConversation(@Param("user1") UUID user1, @Param("user2") UUID user2);

    Optional<ChatMessageEntity> findTopBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtDesc(UUID senderId1, UUID receiverId1, UUID senderId2, UUID receiverId2);

    @Query("""
        SELECT DISTINCT
            CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END
        FROM ChatMessageEntity m
        WHERE m.senderId = :userId OR m.receiverId = :userId
    """)
    List<UUID> findDistinctContactsByUserId(@Param("userId") UUID userId);
}
