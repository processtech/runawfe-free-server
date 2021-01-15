package ru.runa.wfe.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Service
public class MessageTypeService {

    @Autowired
    private ObjectMapper objectMapper;

    @Resource(name = "handlerByMessageType")
    private Map<Class<? extends ChatDto>, ChatSocketMessageHandler<? extends ChatDto>> handlerByMessageType;

    public ChatDto convertJsonToDto(String message) throws IOException {
        return objectMapper.readValue(message, ChatDto.class);
    }

    public ChatSocketMessageHandler<? extends ChatDto> getHandlerByMessageType(Class<? extends ChatDto> dtoClass) {
        return handlerByMessageType.get(dtoClass);
    }
}
