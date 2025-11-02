package az.shopery.model.dto.response;

import az.shopery.model.dto.shared.AuthorDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponseDto {
    UUID id;
    String blogTitle;
    String content;
    Instant createdAt;
    Instant updatedAt;
    String imageUrl;
    Integer likeCount;
    AuthorDto author;
}
