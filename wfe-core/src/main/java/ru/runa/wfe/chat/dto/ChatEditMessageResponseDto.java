package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ChatEditMessageResponseDto extends ChatDto {
    private String messageType;
    private Long messageId;
    private String messageText;

    @JsonGetter("messType")
    public String getMessageType() {
        return messageType;
    }

    @JsonSetter("messType")
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    @JsonGetter("mesId")
    public Long getMessageId() {
        return messageId;
    }

    @JsonSetter("mesId")
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    @JsonGetter("newText")
    public String getMessageText() {
        return messageText;
    }

    @JsonSetter("newText")
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
