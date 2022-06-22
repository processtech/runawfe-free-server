package ru.runa.wfe.chat.config;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import ru.runa.wfe.chat.socket.ChatSocket;
import ru.runa.wfe.chat.socket.HandshakeInterceptor;

/**
 * @author Alekseev Mikhail
 * @since #2451
 */
@Configuration
@EnableWebSocket
@CommonsLog
@Controller
public class WebSocketConfig implements WebSocketConfigurer {
    @Value("${chat.max.message.size.bytes}")
    private int maxMessageSize;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("registerWebSocketHandlers");
        registry.addHandler(socket(), "/").addInterceptors(new HandshakeInterceptor());
    }

    @Bean
    public ChatSocket socket() {
        return new ChatSocket();
    }

    @Bean
    public ServletServerContainerFactoryBean webSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(maxMessageSize);
        container.setMaxBinaryMessageBufferSize(maxMessageSize);
        return container;
    }
}
