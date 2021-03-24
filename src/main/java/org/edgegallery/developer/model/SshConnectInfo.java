package org.edgegallery.developer.model;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import org.springframework.web.socket.WebSocketSession;

public class SshConnectInfo {
    private WebSocketSession webSocketSession;

    private JSch jsch;

    private Channel channel;

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    public JSch getjSch() {
        return jsch;
    }

    public void setjSch(JSch jsch) {
        this.jsch = jsch;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
