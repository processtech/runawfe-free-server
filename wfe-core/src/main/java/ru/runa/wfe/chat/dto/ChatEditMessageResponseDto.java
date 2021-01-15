package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ChatEditMessageResponseDto extends ChatDto {
    private Long messageId;
    private String messageText;

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
