package ru.runa.wfe.chat.coder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.interceptor.Interceptors;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.chat.dto.ChatDto;

@Interceptors({SpringBeanAutowiringInterceptor.class})
public class ChatDtoBinaryDecoder implements Decoder.Binary<ChatDto> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ChatDto decode(ByteBuffer byteBuffer) throws DecodeException {
        ChatDto chatDto;
        try {
            chatDto = objectMapper.readValue(byteBuffer.array(), ChatDto.class);
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
