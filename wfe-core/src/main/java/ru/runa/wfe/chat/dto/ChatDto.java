package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "messageType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatNewMessageDto.class, name = "newMessage"),
        @JsonSubTypes.Type(value = ChatReadMessageDto.class, name = "readMessage"),
        @JsonSubTypes.Type(value = ChatEditMessageDto.class, name = "editMessage"),
        @JsonSubTypes.Type(value = ChatDeleteMessageDto.class, name = "deleteMessage"),
        @JsonSubTypes.Type(value = ChatGetMessagesDto.class, name = "getMessages"),
        @JsonSubTypes.Type(value = ChatErrorMessageDto.class, name = "errorMessage")
})
public class ChatDto {

}
