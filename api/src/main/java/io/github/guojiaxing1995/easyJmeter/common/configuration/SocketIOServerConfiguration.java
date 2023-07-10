package io.github.guojiaxing1995.easyJmeter.common.configuration;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class SocketIOServerConfiguration {

    @Value("${socket.server.host}")
    private String host;

    @Value("${socket.server.port}")
    private int port;

    @Value("${socket.server.enable}")
    private boolean enableSocket;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setOrigin(null);
        config.setAllowCustomRequests(true);

        SocketIOServer server = new SocketIOServer(config);

        if (enableSocket) {
            server.start();
            log.info("SocketIO server started on {}:{}", host, port);
        } else {
            log.info("SocketIO server is disabled.");
        }

        return server;
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }

}
