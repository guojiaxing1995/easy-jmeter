package io.github.guojiaxing1995.easyJmeter.common.configuration;

import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class SocketIOClientConfiguration {

    @Value("${socket.client.serverUrl}")
    private String serverUrl;

    @Value("${socket.client.enable}")
    private boolean enableSocket;

    @Bean
    public Socket socketIOClient() throws URISyntaxException {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.query = "client-type=machine";

        Socket socket = IO.socket(serverUrl, options);

        if (enableSocket) {
            socket.connect();
            log.info("SocketIO client connect to {}", serverUrl);
        } else {
            log.info("SocketIO client is disabled.");
        }

        return socket;
    }
}
