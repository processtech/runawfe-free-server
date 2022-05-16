package ru.runa.wfe.chat;

import org.springframework.stereotype.Component;

@Component
public class ChatExceptionTranslatorImpl implements ChatExceptionTranslator {

    @Override
    public ChatException doTranslate(Throwable exception) {
        if (exception instanceof ChatException) {
            return (ChatException) exception;
        } else {
            return new ChatInternalApplicationException(exception.getMessage());
        }
    }
}