package az.shopery.core_ms.utils.websocket;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class ChatWebSocketRelayHandler extends AbstractWebSocketHandler implements SubProtocolCapable {

    private static final int SEND_TIME_LIMIT_MILLIS = 10000;
    private static final int BUFFER_SIZE_LIMIT_BYTES = 512 * 1024;
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final List<String> STOMP_PROTOCOLS = List.of("v10.stomp", "v11.stomp", "v12.stomp");

    private final String chatMsUrl;
    private final WebSocketClient webSocketClient = new StandardWebSocketClient();
    private final Map<String, WebSocketSession> chatMsSessions = new ConcurrentHashMap<>();

    public ChatWebSocketRelayHandler(@Value("${feign.client.config.chat-ms.url}") String chatMsUrl) {
        this.chatMsUrl = chatMsUrl;
    }

    @Override
    public @NonNull List<String> getSubProtocols() {
        return STOMP_PROTOCOLS;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession clientSession) throws Exception {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        if (Objects.nonNull(clientSession.getAcceptedProtocol()) && !clientSession.getAcceptedProtocol().isBlank()) {
            headers.setSecWebSocketProtocol(clientSession.getAcceptedProtocol());
        }

        WebSocketSession client = new ConcurrentWebSocketSessionDecorator(clientSession, SEND_TIME_LIMIT_MILLIS, BUFFER_SIZE_LIMIT_BYTES);
        WebSocketSession chatMsSession = webSocketClient
                .execute(new ChatMsWebSocketHandler(client), headers, buildChatMsUri((String) clientSession.getAttributes().get("username")))
                .get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        chatMsSessions.put(clientSession.getId(), new ConcurrentWebSocketSessionDecorator(chatMsSession, SEND_TIME_LIMIT_MILLIS, BUFFER_SIZE_LIMIT_BYTES));
        log.info("WS relay opened for session={}", clientSession.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession clientSession, @NonNull TextMessage message) throws Exception {
        relay(clientSession, message);
    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession clientSession, @NonNull BinaryMessage message) throws Exception {
        relay(clientSession, message);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession clientSession, @NonNull CloseStatus status) throws Exception {
        WebSocketSession chatMsSession = chatMsSessions.remove(clientSession.getId());
        if (Objects.nonNull(chatMsSession) && chatMsSession.isOpen()) {
            chatMsSession.close();
        }
        log.info("WS relay closed for session={} with status={}", clientSession.getId(), status);
    }

    private void relay(WebSocketSession clientSession, WebSocketMessage<?> message) throws Exception {
        WebSocketSession chatMsSession = chatMsSessions.get(clientSession.getId());
        if (Objects.isNull(chatMsSession) || !chatMsSession.isOpen()) {
            clientSession.close(CloseStatus.SERVICE_RESTARTED);
            return;
        }
        chatMsSession.sendMessage(message);
    }

    private URI buildChatMsUri(String email) {
        String scheme = chatMsUrl.startsWith("https") ? "wss" : "ws";
        return UriComponentsBuilder.fromUriString(chatMsUrl)
                .scheme(scheme)
                .path("/ws")
                .queryParam("email", "{email}")
                .encode()
                .buildAndExpand(email)
                .toUri();
    }
}
