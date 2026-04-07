package az.shopery.utils.websocket;

import az.shopery.utils.security.JwtService;
import java.security.Principal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (Objects.isNull(accessor)) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Principal existingUser = accessor.getUser();

            log.info("STOMP CONNECT received, existing user={}", Objects.nonNull(existingUser) ? existingUser.getName() : "NULL");

            if (Objects.nonNull(existingUser)) {
                return message;
            }

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (Objects.isNull(authHeader)) {
                authHeader = accessor.getFirstNativeHeader("authorization");
            }

            if (Objects.nonNull(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);

                if (Objects.nonNull(username) && !username.isBlank()) {
                    accessor.setUser(() -> username);
                    log.info("STOMP CONNECT authenticated from native header user={}", username);
                    return message;
                }
            }
        }
        return message;
    }
}
