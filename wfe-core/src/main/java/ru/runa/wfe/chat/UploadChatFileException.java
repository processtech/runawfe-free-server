package ru.runa.wfe.chat;

/**
 * @author Sergey Inyakin
 */

public class UploadChatFileException extends ChatException{
    private static final String MESSAGE = "File upload error";

    public UploadChatFileException(){
        super(MESSAGE);
    }

    public UploadChatFileException(Throwable cause){
        super(MESSAGE, cause);
    }

    public UploadChatFileException(String message){
        super(message);
    }

    public UploadChatFileException(String message, Throwable cause){
        super(message, cause);
    }
}
