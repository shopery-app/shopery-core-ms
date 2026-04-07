package az.shopery.utils.websocket;

import az.shopery.utils.security.JwtService;
import io.jsonwebtoken.JwtException;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes
    ) {
        log.info("WS beforeHandshake called");

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.warn("WS rejected: not a servlet request");
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        String token = servletRequest.getServletRequest().getParameter("token");
        log.info("WS token present: {}", token != null && !token.isBlank());

        if (Objects.isNull(token) || token.isBlank()) {
            log.warn("WS rejected: missing token");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            String email = jwtService.extractUsername(token);

            if (Objects.isNull(email) || email.isBlank()) {
                log.warn("WS rejected: invalid email extracted from token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("username", email);
            log.info("WS handshake accepted for user={}", email);
            return true;
        } catch (JwtException e) {
            log.warn("WS rejected: invalid or expired JWT - {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        } catch (Exception e) {
            log.error("WS rejected: unexpected handshake failure", e);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception
    ) {}
}
