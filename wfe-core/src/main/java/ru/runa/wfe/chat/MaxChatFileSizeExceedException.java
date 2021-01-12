package ru.runa.wfe.chat;

/**
 * @author Sergey Inyakin
 */

public class MaxChatFileSizeExceedException extends ChatException{

    private static final String MESSAGE = "File exceeds the maximum size";

    public MaxChatFileSizeExceedException(){
        super(MESSAGE);
    }

    public MaxChatFileSizeExceedException(Throwable cause){
        super(MESSAGE, cause);
    }
}
