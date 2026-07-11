package com.chat.ws_server;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
/**
 * 
 * WebSocketConfig
 */
@Configuration
@EnableWebSocket   
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatHandler chatHandler;
    public WebSocketConfig(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(chatHandler, "/chat")
            .setAllowedOrigins("http://localhost:5173");
    }
}
