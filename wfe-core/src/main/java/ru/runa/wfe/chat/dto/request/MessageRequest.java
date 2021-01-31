package ru.runa.wfe.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;
import ru.runa.wfe.chat.dto.ChatDto;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "messageType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddMessageRequest.class, name = "newMessage"),
        @JsonSubTypes.Type(value = EditMessageRequest.class, name = "editMessage"),
        @JsonSubTypes.Type(value = DeleteMessageRequest.class, name = "deleteMessage"),
        @JsonSubTypes.Type(value = ReadMessageRequest.class, name = "readMessage")
})
public class MessageRequest extends ChatDto {
}
