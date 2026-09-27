package az.shopery.core_ms.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogRequestDto {
    @NotBlank(message = "Title cannot be empty!")
    @Size(max = 40, message = "Title is too long.")
    String title;
    @NotBlank(message = "Content cannot be empty!")
    @Size(max = 400, message = "Content is too long.")
    String content;
}
