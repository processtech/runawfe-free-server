package ru.runa.wfe.chat;

import lombok.Getter;

@Getter
public class ChatInternalApplicationException extends ChatException {
    
    private final int errorCode = 3001;

    public ChatInternalApplicationException(String message) {
        super(message);
    }
}
