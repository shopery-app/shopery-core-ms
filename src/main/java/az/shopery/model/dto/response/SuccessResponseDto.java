package az.shopery.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponseDto<T> {
    @Builder.Default
    HttpStatus status = HttpStatus.OK;
    @Builder.Default
    int statusCode = HttpStatus.OK.value();
    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();
    String message;
    T data;

    public static SuccessResponseDto<Void> of(String message) {
        return SuccessResponseDto.<Void>builder()
                .message(message)
                .build();
    }

    public static <T> SuccessResponseDto<T> of(T data, String message) {
        return SuccessResponseDto.<T>builder()
                .message(message)
                .data(data)
                .build();
    }
}
