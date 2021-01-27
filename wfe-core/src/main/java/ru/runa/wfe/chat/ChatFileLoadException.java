package ru.runa.wfe.chat;

/**
 * @author Sergey Inyakin
 */
public class ChatFileLoadException extends ChatException {
    public ChatFileLoadException(ChatMessageFile file) {
        super("File load error: " + file.getFileName());
    }
}
