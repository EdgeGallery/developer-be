package org.edgegallery.developer.util.webssh.interceptor;

import java.util.Map;
import java.util.UUID;
import org.edgegallery.developer.util.webssh.constant.ConstantPool;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class WebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse,
        WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        if (serverHttpRequest instanceof ServletServerHttpRequest) {
            //生成一个UUID
            String uuid = UUID.randomUUID().toString().replace("-", "");
            //将uuid放到websocketsession中
            map.put(ConstantPool.USER_UUID_KEY, uuid);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse,
        WebSocketHandler webSocketHandler, Exception e) {

    }
}
