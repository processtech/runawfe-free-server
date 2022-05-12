package ru.runa.wfe.chat.dto.broadcast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import ru.runa.wfe.chat.dto.AbstractChatDto;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "messageType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageAddedBroadcast.class, name = "newMessage"),
        @JsonSubTypes.Type(value = MessageDeletedBroadcast.class, name = "deleteMessage"),
        @JsonSubTypes.Type(value = MessageEditedBroadcast.class, name = "editMessage"),
        @JsonSubTypes.Type(value = ErrorMessageBroadcast.class, name = "errorMessage")
})
public abstract class MessageBroadcast extends AbstractChatDto implements Serializable {
    private static final long serialVersionUID = 3803247808782939805L;

    public MessageBroadcast(Long processId) {
        super(processId);
    }
}
