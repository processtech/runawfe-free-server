package ru.runa.wfe.chat;

/**
 * @author Sergey Inyakin
 */
public class ChatException extends RuntimeException {
    public ChatException(String message) {
        super(message, null, false, false);
    }
}
