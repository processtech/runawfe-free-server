package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ChatDeleteMessageDto extends ChatDto {
    private Long messageId;

    @JsonGetter("messageId")
    public Long getMessageId() {
        return messageId;
    }

    @JsonSetter("messageId")
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

}
