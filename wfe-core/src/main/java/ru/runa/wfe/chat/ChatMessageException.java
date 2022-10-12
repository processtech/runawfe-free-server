package ru.runa.wfe.chat;

/**
 * @author Sergey Inyakin
 */
public class ChatMessageException extends ChatInternalApplicationException {

    public ChatMessageException(String message) {
        super(message);
    }
}