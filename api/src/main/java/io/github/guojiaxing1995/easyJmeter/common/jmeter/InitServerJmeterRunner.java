package io.github.guojiaxing1995.easyJmeter.common.jmeter;

import io.github.guojiaxing1995.easyJmeter.common.util.ZipUtil;
import io.github.guojiaxing1995.easyJmeter.service.MachineService;
import org.apache.commons.compress.archivers.ArchiveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class InitServerJmeterRunner implements ApplicationRunner {

    @Value("${socket.server.enable}")
    private boolean enableSocket;

    @Autowired
    private MachineService machineService;
    @Override
    public void run(ApplicationArguments args) throws IOException, ArchiveException {
        if (enableSocket) {
            // 初始化服务端jmeter相关配置
            ClassPathResource resource = new ClassPathResource("apache-jmeter.zip");
            InputStream inputStream = resource.getInputStream();
            String targetDirectory = System.getProperty("user.dir");
            ZipUtil.unzipFile(inputStream, targetDirectory);

            // 启动后重置所有压力机为下线状态
            machineService.setMachineOffline();
        }
    }
}
