package ru.runa.wfe.chat.dto.request;

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
        @JsonSubTypes.Type(value = AddMessageRequest.class, name = "newMessage"),
        @JsonSubTypes.Type(value = EditMessageRequest.class, name = "editMessage"),
        @JsonSubTypes.Type(value = DeleteMessageRequest.class, name = "deleteMessage")
})
public abstract class MessageRequest extends AbstractChatDto implements Serializable {
    private static final long serialVersionUID = 4917458326971028795L;
}
