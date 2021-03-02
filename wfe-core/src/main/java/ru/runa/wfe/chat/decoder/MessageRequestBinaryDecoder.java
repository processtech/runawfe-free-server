package ru.runa.wfe.chat.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.interceptor.Interceptors;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.chat.config.ChatBean;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;

@Interceptors({ SpringBeanAutowiringInterceptor.class })
public class MessageRequestBinaryDecoder implements Decoder.Binary<MessageRequest> {

    @Autowired
    @ChatBean
    private ObjectMapper objectMapper;

    @Override
    public MessageRequest decode(ByteBuffer byteBuffer) throws DecodeException {
        try {
            return objectMapper.readValue(byteBuffer.array(), MessageRequest.class);
        } catch (IOException e) {
            throw new DecodeException(byteBuffer, e.getMessage(), e);
        }
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
