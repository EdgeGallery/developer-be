package org.edgegallery.developer.config;

import org.edgegallery.developer.util.webssh.interceptor.WebSocketInterceptor;
import org.edgegallery.developer.util.webssh.websocket.WebSshWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSshWebSocketConfig implements WebSocketConfigurer {
    @Autowired
    WebSshWebSocketHandler webSshWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        //socket通道
        //指定处理器和路径
        webSocketHandlerRegistry.addHandler(webSshWebSocketHandler, "/webssh")
            .addInterceptors(new WebSocketInterceptor()).setAllowedOrigins("*");
    }
}
