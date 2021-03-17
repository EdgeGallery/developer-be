package org.edgegallery.developer.service;

import java.io.IOException;
import org.springframework.web.socket.WebSocketSession;

public interface WebSshService {

    void initConnection(WebSocketSession session);

    void recvHandle(String buffer, WebSocketSession session);

    void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;

    void close(WebSocketSession session);
}
