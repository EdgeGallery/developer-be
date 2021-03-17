package org.edgegallery.developer.service;

import java.io.IOException;
import org.springframework.web.socket.WebSocketSession;

public interface WebSshService {

    public void initConnection(WebSocketSession session);

    public void recvHandle(String buffer, WebSocketSession session);

    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;

    public void close(WebSocketSession session);
}
