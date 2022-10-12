package ru.runa.wfe.chat.dto.broadcast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import ru.runa.wfe.chat.dto.AbstractChatDto;
import ru.runa.wfe.chat.dto.ServerMessage;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class MessageBroadcast extends AbstractChatDto implements ServerMessage {
    private static final long serialVersionUID = 3803247808782939805L;

    public MessageBroadcast(Long processId) {
        super(processId);
    }
}
