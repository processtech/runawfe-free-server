package ru.runa.wfe.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.runa.wfe.chat.dto.*;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ChatConfig {

    @Bean
    public List<Class<? extends ChatDto>> dtoClasses() {
        List<Class<? extends ChatDto>> dtoClasses = new ArrayList<>();

        dtoClasses.add(ChatNewMessageDto.class);
        dtoClasses.add(ChatReadMessageDto.class);
        dtoClasses.add(ChatEditMessageDto.class);
        dtoClasses.add(ChatDeleteMessageDto.class);
        dtoClasses.add(ChatGetMessagesDto.class);

        return dtoClasses;
    }

    @Bean
    public Map<Class<? extends ChatDto>, ChatSocketMessageHandler<? extends ChatDto>> handlerByMessageType(
            List<ChatSocketMessageHandler<? extends ChatDto>> handlers) {
        Map<Class<? extends ChatDto>, ChatSocketMessageHandler<? extends ChatDto>> handlersByMessageType = new HashMap<>();

        for (ChatSocketMessageHandler<? extends ChatDto> handler : handlers) {
            for (Class<? extends ChatDto> dtoClass : dtoClasses()) {
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
}
