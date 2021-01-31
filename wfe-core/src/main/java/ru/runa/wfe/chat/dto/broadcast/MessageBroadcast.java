package ru.runa.wfe.chat.dto.broadcast;

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
        @JsonSubTypes.Type(value = AddedMessageBroadcast.class, name = "newMessage"),
        @JsonSubTypes.Type(value = DeletedMessageBroadcast.class, name = "deleteMessage"),
        @JsonSubTypes.Type(value = EditedMessageBroadcast.class, name = "editMessage"),
        @JsonSubTypes.Type(value = ErrorMessageBroadcast.class, name = "errorMessage")
})
public class MessageBroadcast extends ChatDto {
}
