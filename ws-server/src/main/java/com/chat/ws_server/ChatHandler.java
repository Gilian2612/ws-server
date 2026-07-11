package com.chat.ws_server;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.logstash.logback.argument.StructuredArguments;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
/**
 * ChatHandler — gestiona el ciclo de vida de cada sesión WebSocket.
 * Incluye: métricas (Micrometer), trazas (Zipkin) y logs estructurados (Logstash).
 */
@Component
public class ChatHandler extends TextWebSocketHandler {
    private static final Logger log =
            LoggerFactory.getLogger(ChatHandler.class);
    private final Set<WebSocketSession> sessions =
            new CopyOnWriteArraySet<>();
    private final Counter      messagesReceived;
    private final Counter      messagesBroadcast;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Tracer       tracer;
    public ChatHandler(MeterRegistry registry, Tracer tracer) {
        this.tracer = tracer;

        /**
         * Contador de mensajes recibidos 
         */
        this.messagesReceived = Counter.builder("ws.messages.received")
                .description("Total de mensajes recibidos del cliente")
                .register(registry);
        /**
         * Contador de mensajes enviados 
         */
        this.messagesBroadcast = Counter.builder("ws.messages.broadcast")
                .description("Total de mensajes enviados a clientes")
                .register(registry);

        Gauge.builder("ws.sessions.active", sessions, Set::size)
                .description("Sesiones WebSocket activas en este momento")
                .register(registry);
    }
    /**
     * Cuando cliente se conecta llama 
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WS_CONNECTED",
            StructuredArguments.kv("session_id", session.getId()), StructuredArguments.kv("active_sessions", sessions.size()));
        broadcast(new ChatMessage("system", "Un usuario se conectó", sessions.size()));
    }
    /**
     * Cuando llega un mensaje de texto 
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Span span = tracer.nextSpan()
                .name("ws.handle-message")
                .tag("session.id", session.getId())
                .start();
        /**
         * Cierra el scope 
         */
        try (Tracer.SpanInScope scope = tracer.withSpan(span)) {

            messagesReceived.increment();
            ChatMessage incoming = mapper.readValue( message.getPayload(), ChatMessage.class);

            span.tag("chat.username",     incoming.getUsername());
            span.tag("chat.text_length",  String.valueOf(incoming.getText().length()));
            span.tag("chat.active_users", String.valueOf(sessions.size()));

            log.info("WS_MESSAGE",StructuredArguments.kv("username", incoming.getUsername()), StructuredArguments.kv("message_length",  incoming.getText().length()), StructuredArguments.kv("active_sessions", sessions.size()));

            ChatMessage outgoing = new ChatMessage();
            outgoing.setType("message");
            outgoing.setUsername(incoming.getUsername());
            outgoing.setText(incoming.getText());
            outgoing.setTimestamp(Instant.now().toString());
            broadcast(outgoing);
        } catch (Exception e) {
            span.error(e); 
            System.err.println("Error procesando mensaje: " + e.getMessage());
        } finally {
            span.end();
        }
    }

    /**
     * Cliente se desconecta 
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WS_DISCONNECTED", StructuredArguments.kv("session_id", session.getId()), StructuredArguments.kv("close_status", status.getCode()), StructuredArguments.kv("active_sessions", sessions.size()) );
        broadcast(new ChatMessage("system", "Un usuario se desconectó", sessions.size()));
    }
    /**
     * @param msg
     * @throws Exception
     */
    private void broadcast(ChatMessage msg) throws Exception {
        String json = mapper.writeValueAsString(msg);
        TextMessage frame = new TextMessage(json);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                synchronized (s) { s.sendMessage(frame); }
                messagesBroadcast.increment();
            }
        }
    }
}
