package org.edgegallery.developer.service;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


public interface WebSSHService {

    public void initConnection(WebSocketSession session);


    public void recvHandle(String buffer, WebSocketSession session);


    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;


    public void close(WebSocketSession session);
}
