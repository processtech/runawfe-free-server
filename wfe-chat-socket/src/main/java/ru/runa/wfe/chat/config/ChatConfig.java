package ru.runa.wfe.chat.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.runa.wfe.chat.ChatExceptionTranslator;
import ru.runa.wfe.chat.ChatExceptionTranslatorImpl;
import ru.runa.wfe.chat.ChatLocalizationService;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.handler.ChatSocketMessageHandler;

/**
 * @author Alekseev Mikhail
 * @since #2451
 */
@Configuration
@EnableScheduling
@ComponentScan({"ru.runa.wfe.chat.handler", "ru.runa.wfe.chat.socket"})
public class ChatConfig {
    @Bean
    public Map<Class<? extends MessageRequest>, ChatSocketMessageHandler<? extends MessageRequest>> handlerByMessageType(
            List<ChatSocketMessageHandler<? extends MessageRequest>> handlers) {
        Map<Class<? extends MessageRequest>, ChatSocketMessageHandler<? extends MessageRequest>> handlersByMessageType = new HashMap<>();

        for (ChatSocketMessageHandler<? extends MessageRequest> handler : handlers) {
            handlersByMessageType.put(handler.getRequestType(), handler);
        }
        return handlersByMessageType;
    }

    @Bean
    public TaskScheduler chatTaskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("chat-scheduler-");
        scheduler.setDaemon(true);
        return scheduler;
    }

    @Bean
    public ChatExceptionTranslator translator() {
        return new ChatExceptionTranslatorImpl();
    }

    @Bean
    public ChatLocalizationService localizationService() {
        return new ChatLocalizationService();
    }
}
