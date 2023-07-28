package ru.runa.wfe.chat.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.chat.dto.ClientMessage;
import ru.runa.wfe.chat.dto.ServerMessage;

/**
 * @author Alekseev Mikhail
 * @since #2451
 */
@Component
@CommonsLog
public class MessageRequestBinaryConverter {
    private final ObjectMapper objectMapper;

    public MessageRequestBinaryConverter() {
        this.objectMapper = new ObjectMapper();
    }

    public ClientMessage decode(ByteBuffer byteBuffer) throws IOException {
        return objectMapper.readValue(new ByteBufferBackedInputStream(byteBuffer), ClientMessage.class);
    }

    public TextMessage encode(ServerMessage dto) {
        try {
            return new TextMessage(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            log.error("Unable to encode " + dto, e);
            throw new InternalApplicationException("Unable to encode " + dto, e);
        }
    }
}
