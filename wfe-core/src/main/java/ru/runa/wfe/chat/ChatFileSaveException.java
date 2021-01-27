package ru.runa.wfe.chat;

/**
 * @author Sergey Inyakin
 */
public class ChatFileSaveException extends ChatException {
    public ChatFileSaveException(ChatMessageFile file) {
        super("File save error: " + file.getFileName());
    }
}
