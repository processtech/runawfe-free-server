package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import ru.runa.wfe.chat.dto.broadcast.AuthenticationRequired;
import ru.runa.wfe.chat.dto.broadcast.ErrorMessageBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "messageType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageAddedBroadcast.class, name = "newMessage"),
        @JsonSubTypes.Type(value = MessageDeletedBroadcast.class, name = "deleteMessage"),
        @JsonSubTypes.Type(value = MessageEditedBroadcast.class, name = "editMessage"),
        @JsonSubTypes.Type(value = ErrorMessageBroadcast.class, name = "errorMessage"),
        @JsonSubTypes.Type(value = AuthenticationRequired.class, name = "authenticationRequired")
})
public interface ServerMessage extends Serializable {
}
