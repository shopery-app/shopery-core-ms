package az.shopery.core_ms.utils.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Slf4j
@RequiredArgsConstructor
public class ChatMsWebSocketHandler extends AbstractWebSocketHandler {

    private final WebSocketSession clientSession;

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession chatMsSession, @NonNull TextMessage message) throws Exception {
        clientSession.sendMessage(message);
    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession chatMsSession, @NonNull BinaryMessage message) throws Exception {
        clientSession.sendMessage(message);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession chatMsSession, @NonNull CloseStatus status) throws Exception {
        if (clientSession.isOpen()) {
            clientSession.close();
        }
        log.info("chat-ms WS closed for client session={} with status={}", clientSession.getId(), status);
    }
}
