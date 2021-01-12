package ru.runa.wfe.chat;

import ru.runa.wfe.InternalApplicationException;

/**
 * @author Sergey Inyakin
 */

public class ChatException extends InternalApplicationException {

    public ChatException(){
        super();
    }

    public ChatException(String message){
        super(message);
    }

    public ChatException(Throwable cause){
        super(cause);
    }

    public ChatException(String message, Throwable cause){
        super(message, cause);
    }

}
