package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.ReadMessageRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "messageType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddMessageRequest.class, name = "newMessage"),
        @JsonSubTypes.Type(value = ReadMessageRequest.class, name = "readMessage"),
        @JsonSubTypes.Type(value = EditMessageRequest.class, name = "editMessage"),
        @JsonSubTypes.Type(value = DeleteMessageRequest.class, name = "deleteMessage"),
        @JsonSubTypes.Type(value = ChatErrorMessageDto.class, name = "errorMessage")
})
@Getter
@Setter
public class ChatDto {
    private Long processId;

}
