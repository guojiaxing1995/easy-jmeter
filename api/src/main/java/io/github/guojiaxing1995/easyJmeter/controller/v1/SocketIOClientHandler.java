package io.github.guojiaxing1995.easyJmeter.controller.v1;

import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SocketIOClientHandler {

    private final Socket socket;

    @Autowired
    public SocketIOClientHandler(Socket socket){
        this.socket = socket;
        connectListener();
        disconnectListener();
        eventListeners();
    }

    //事件监听
    private void connectListener() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            log.info("Connected to server");
        });
    }

    private void disconnectListener() {
        socket.on(Socket.EVENT_DISCONNECT, args -> {
            log.info("Socket disconnect");
        });
    }

    private void eventListeners() {
        socket.on("msgClient", args -> {
            log.info(args[0].toString());
            socket.emit("msgServer", "收到" + args[0].toString());
        });
    }
}
