package ru.runa.wfe.chat;

public abstract class ChatException extends RuntimeException {

    public ChatException(String message) {
        super(message, null, false, false);
    }
    
    public abstract int getErrorCode();

}
