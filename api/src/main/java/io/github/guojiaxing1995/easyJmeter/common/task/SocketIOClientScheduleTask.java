package io.github.guojiaxing1995.easyJmeter.common.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.MachineOnlineEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.BasicProperties;
import io.github.guojiaxing1995.easyJmeter.dto.machine.HeartBeatMachineDTO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "socket.client.enable", havingValue = "true")
@Slf4j
public class SocketIOClientScheduleTask {

    private final Socket socket;

    public SocketIOClientScheduleTask(Socket socket) {
        this.socket = socket;
    }

    @Scheduled(cron = "${cron:0 0/1 * * * ? }")
    public void heartBeat() throws JsonProcessingException {
        log.info("心跳");
        ObjectMapper mapper = new ObjectMapper();
        BasicProperties basicProperties = new BasicProperties();
        HeartBeatMachineDTO heartBeatMachineDTO = new HeartBeatMachineDTO(null,basicProperties.getAddress(),basicProperties.getPath(),basicProperties.getVersion(), MachineOnlineEnum.ONLINE);
        String heartBeat = mapper.writeValueAsString(heartBeatMachineDTO);
        socket.emit("heartBeat", heartBeat);
    }
}
