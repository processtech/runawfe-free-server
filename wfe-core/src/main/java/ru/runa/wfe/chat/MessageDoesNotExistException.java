package ru.runa.wfe.chat;

/**
 * @author Alekseev Mikhail
 * @since #2199
 */
public class MessageDoesNotExistException extends ChatException {
    public MessageDoesNotExistException(Long messageId) {
        super("Message " + messageId + " does not exist");
    }
}
