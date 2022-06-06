package ru.runa.wfe.chat;

public interface ChatExceptionTranslator {

    ChatException doTranslate(Throwable exception);

}