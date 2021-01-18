package ru.runa.wfe.chat.dto;

import com.fasterxml.jackson.annotation.JsonGetter;

public class ChatErrorMessageDto extends ChatDto{

    private String message;

    public ChatErrorMessageDto(String message) {
        this.message = message;
    }

    @JsonGetter("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
