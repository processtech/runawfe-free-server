package ru.runa.wfe.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.runa.wfe.chat.mapper.AddMessageRequestMapper;
import ru.runa.wfe.chat.mapper.ChatMessageFileDetailMapper;
import ru.runa.wfe.chat.mapper.ChatMessageFileMapper;
import ru.runa.wfe.chat.mapper.MessageAddedBroadcastFileMapper;
import ru.runa.wfe.chat.mapper.MessageAddedBroadcastMapper;

@Configuration
public class ChatConfig {
    @Bean
    public ChatMessageFileMapper chatMessageFileMapper() {
        return new ChatMessageFileMapper();
    }

    @Bean
    public ChatMessageFileDetailMapper chatMessageFileDetailMapper() {
        return new ChatMessageFileDetailMapper();
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
