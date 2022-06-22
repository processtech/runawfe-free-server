package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "messageType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddMessageRequest.class, name = "newMessage"),
        @JsonSubTypes.Type(value = EditMessageRequest.class, name = "editMessage"),
        @JsonSubTypes.Type(value = DeleteMessageRequest.class, name = "deleteMessage"),
        @JsonSubTypes.Type(value = TokenMessage.class, name = "tokenMessage")
})
public interface ClientMessage extends Serializable {
}
