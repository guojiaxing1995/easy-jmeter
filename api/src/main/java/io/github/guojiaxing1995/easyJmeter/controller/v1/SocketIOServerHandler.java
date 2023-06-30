package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SocketIOServerHandler {

    @Autowired
    private SocketIOServer socketServer;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.info("Client connected: " + client.getSessionId());
        // 处理连接事件
        // ...
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("Client disconnected: " + client.getSessionId());
        // 处理断开连接事件
        // ...
    }

    @OnEvent("msgServer")
    public void handleMsgEvent(SocketIOClient client, String message) {
        // 处理 chat 事件的逻辑
        // ...
        log.info("msg: " + message);
        client.sendEvent("msgClient", "已经收到" + message);

    }

}
