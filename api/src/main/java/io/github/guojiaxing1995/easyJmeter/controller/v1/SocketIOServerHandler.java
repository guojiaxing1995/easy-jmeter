package io.github.guojiaxing1995.easyJmeter.controller.v1;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import io.github.guojiaxing1995.easyJmeter.dto.machine.HeartBeatMachineDTO;
import io.github.guojiaxing1995.easyJmeter.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SocketIOServerHandler {

    @Autowired
    private SocketIOServer socketServer;

    @Autowired
    private MachineService machineService;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.info("Client connected: " + client.getSessionId());
        // 处理连接事件
        // 判断客户端类型 加入room
        HandshakeData handshakeData = client.getHandshakeData();
        String type = handshakeData.getUrlParams().get("client-type").get(0);
        if (type.equals("machine")){
            client.joinRoom("machine");
        } else if (type.equals("web")) {
            client.joinRoom("web");
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("Client disconnected: " + client.getSessionId());
        // 处理断开连接事件
        // 压力机客户端离线后处理
        if (machineService.getByClientId(client.getSessionId().toString()) != null){
            // 设置为已下线状态
            HeartBeatMachineDTO heartBeatMachineDTO = new HeartBeatMachineDTO(client.getSessionId().toString());
            machineService.setMachineStatus(heartBeatMachineDTO, MachineOnlineEnum.OFFLINE);
            log.info("压力机已经离线:" + client.getSessionId());
        }

    }

    @OnEvent("msgServer")
    public void handleMsgEvent(SocketIOClient client, String message) {
        // 处理 chat 事件的逻辑
        // ...
        log.info("msg: " + message);
        client.sendEvent("msgClient", "已经收到" + message);

    }

    @OnEvent("heartBeat")
    public void  handleHeartBeatEvent(SocketIOClient client, String heartBeat) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HeartBeatMachineDTO heartBeatMachineDTO = mapper.readValue(heartBeat, HeartBeatMachineDTO.class);
        heartBeatMachineDTO.setClientId(client.getSessionId().toString());
        machineService.setMachineStatus(heartBeatMachineDTO, MachineOnlineEnum.ONLINE);

        BroadcastOperations web = socketServer.getRoomOperations("web");
        web.sendEvent("machineHeartWeb", "hello");
    }

}
