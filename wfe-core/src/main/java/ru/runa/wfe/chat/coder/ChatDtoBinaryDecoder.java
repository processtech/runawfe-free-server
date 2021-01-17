package ru.runa.wfe.chat.coder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.service.MessageTypeService;

import javax.interceptor.Interceptors;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Interceptors({ SpringBeanAutowiringInterceptor.class })
public class ChatDtoBinaryDecoder implements Decoder.Binary<ChatDto> {

    @Autowired
    private MessageTypeService messageTypeService;

    @Override
    public ChatDto decode(ByteBuffer byteBuffer) throws DecodeException {
        ChatDto chatDto;
        try {
            chatDto = messageTypeService.convertJsonToDto(
                    new String(byteBuffer.array(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new DecodeException(byteBuffer, e.getMessage());
        }
        return chatDto;
    }

    @Override
    public boolean willDecode(ByteBuffer byteBuffer) {
        return byteBuffer.hasArray();
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
