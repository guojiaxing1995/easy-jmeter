package io.github.guojiaxing1995.easyJmeter.common.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.JmeterExternal;
import io.github.guojiaxing1995.easyJmeter.dto.machine.HeartBeatMachineDTO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "socket.client.enable", havingValue = "true")
@Slf4j
public class AgentScheduleTask {

    private final Socket socket;

    public AgentScheduleTask(Socket socket) {
        this.socket = socket;
    }

    @Scheduled(cron = "${cron.heartBeat:0 0/1 * * * ? }")
    public void heartBeat() throws JsonProcessingException {
        log.info("心跳");
        JmeterExternal jmeterExternal = new JmeterExternal(socket);
        HeartBeatMachineDTO heartBeatMachineDTO = new HeartBeatMachineDTO(null,jmeterExternal.getAddress(),jmeterExternal.getPath(),jmeterExternal.getVersion(), true);
        String heartBeat = new ObjectMapper().writeValueAsString(heartBeatMachineDTO);
        socket.emit("heartBeat", heartBeat);
    }

    @Scheduled(cron = "${cron.deleteTaskRecordStorage:0 0 1 ? * MON }")
    public void deleteTaskRecordStorage() {
        String directoryPath = System.getenv("JMETER_HOME");
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("TASK"))
                    .forEach(path -> {
                        try (Stream<Path> files = Files.walk(path)) {
                            files.sorted(Comparator.reverseOrder())
                                    .map(Path::toFile)
                                    .forEach(File::delete);
                        } catch (IOException e) {
                            log.error("删除任务记录失败", e);
                        }
                    });
        } catch (IOException e) {
            log.error("删除任务记录失败", e);
        }
        log.info("删除历史Task运行数据");
    }
}
