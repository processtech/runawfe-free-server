package ru.runa.wfe.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.dto.request.ReadMessageRequest;
import ru.runa.wfe.chat.mapper.ChatMessageFileMapper;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;

@Configuration
public class ChatConfig {

    @Bean
    public List<Class<? extends MessageRequest>> messageRequestClasses() {
        return Arrays.asList(
                AddMessageRequest.class,
                ReadMessageRequest.class,
                EditMessageRequest.class,
                DeleteMessageRequest.class
        );
    }

    @Bean
    public Map<Class<? extends MessageRequest>, ChatSocketMessageHandler<? extends MessageRequest, ? extends MessageBroadcast>> handlerByMessageType(
            List<ChatSocketMessageHandler<? extends MessageRequest, ? extends MessageBroadcast>> handlers) {
        Map<Class<? extends MessageRequest>, ChatSocketMessageHandler<? extends MessageRequest, ? extends MessageBroadcast>> handlersByMessageType = new HashMap<>();

        for (ChatSocketMessageHandler<? extends MessageRequest, ? extends MessageBroadcast> handler : handlers) {
            for (Class<? extends MessageRequest> dtoClass : messageRequestClasses()) {
                if (handler.isSupports(dtoClass)) {
                    handlersByMessageType.put(dtoClass, handler);
                    break;
                }
            }
        }
        return handlersByMessageType;
    }

    @Bean
    public ObjectMapper chatObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ChatMessageFileMapper chatMessageFileMapper() {
        return new ChatMessageFileMapper();
    }
}
