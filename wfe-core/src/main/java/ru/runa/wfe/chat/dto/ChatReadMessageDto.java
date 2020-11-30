package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ChatReadMessageDto extends ChatDto {
    private Long currentMessageId;

    @JsonGetter("currentMessageId")
    public Long getCurrentMessageId() {
        return currentMessageId;
    }

    @JsonSetter("currentMessageId")
    public void setCurrentMessageId(Long currentMessageId) {
        this.currentMessageId = currentMessageId;
    }

}
