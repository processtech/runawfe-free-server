package ru.runa.wfe.chat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChatExceptionTranslatorTest {

    private ChatExceptionTranslator translator = new ChatExceptionTranslatorImpl();

    @Test
    public void doTranslateTest() {

        assertTrue(translator.doTranslate(new RuntimeException()).getClass() == ChatInternalApplicationException.class);
        assertTrue(translator.doTranslate(new ChatMessageException("Some ChatMessageException")).getClass() == ChatMessageException.class);
        assertTrue(translator.doTranslate(new ChatFileIoException("Some ChatFileIoException")).getClass() == ChatFileIoException.class);
        assertEquals(translator.doTranslate(new ChatInternalApplicationException("Message Exception")).getErrorCode(), 3001);
        assertEquals(translator.doTranslate(new ChatMessageException("Some message Exception")).getErrorCode(), 3001);
        assertEquals(translator.doTranslate(new ChatFileIoException("Some file IO Exception")).getErrorCode(), 3001);
    }
}