package ru.runa.wfe.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.mapper.ActorToLongMapper;
import ru.runa.wfe.chat.mapper.AddMessageRequestMapper;
import ru.runa.wfe.chat.mapper.ChatMessageFileDetailMapper;
import ru.runa.wfe.chat.mapper.ChatMessageFileMapper;
import ru.runa.wfe.chat.mapper.MessageAddedBroadcastFileMapper;
import ru.runa.wfe.chat.mapper.MessageAddedBroadcastMapper;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
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
    public ObjectMapper chatObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ChatMessageFileMapper chatMessageFileMapper() {
        return new ChatMessageFileMapper();
    }

    @Bean
    public ChatMessageFileDetailMapper chatMessageFileDetailMapper() {
        return new ChatMessageFileDetailMapper();
    }

    @Bean
    public ActorToLongMapper actorToLongMapper() {
        return new ActorToLongMapper();
    }

    @Bean
    public MessageAddedBroadcastMapper messageAddedBroadcastMapper() {
        return new MessageAddedBroadcastMapper();
    }

    @Bean
    public MessageAddedBroadcastFileMapper messageAddedBroadcastFileMapper() {
        return new MessageAddedBroadcastFileMapper();
    }

    @Bean
    public AddMessageRequestMapper addMessageRequestMapper() {
        return new AddMessageRequestMapper();
    }
}
