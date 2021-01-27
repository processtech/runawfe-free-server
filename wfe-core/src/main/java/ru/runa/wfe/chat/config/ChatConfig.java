package ru.runa.wfe.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.runa.wfe.chat.dto.ChatDeleteMessageDto;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.dto.ChatEditMessageDto;
import ru.runa.wfe.chat.dto.ChatGetMessagesDto;
import ru.runa.wfe.chat.dto.ChatNewMessageDto;
import ru.runa.wfe.chat.dto.ChatReadMessageDto;
import ru.runa.wfe.chat.maper.ChatMessageFileMapper;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;

@Configuration
public class ChatConfig {

    @Bean
    public List<Class<? extends ChatDto>> dtoClasses() {
        return Arrays.asList(
                ChatNewMessageDto.class,
                ChatReadMessageDto.class,
                ChatEditMessageDto.class,
                ChatDeleteMessageDto.class,
                ChatGetMessagesDto.class
        );
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

    @Bean
    public ChatMessageFileMapper chatMessageFileMapper() {
        return new ChatMessageFileMapper();
    }
}
