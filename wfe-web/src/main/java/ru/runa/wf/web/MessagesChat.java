package ru.runa.wf.web;

import ru.runa.common.web.StrutsMessage;

public final class MessagesChat {

    private MessagesChat() {
    }    

    public static final StrutsMessage SEND_MESSAGE = new StrutsMessage("chat.message.send");
    public static final StrutsMessage PRIVATE_MESSAGE = new StrutsMessage("chat.message.private");
    public static final StrutsMessage PLACEHOLDER_MESSAGE = new StrutsMessage("chat.message.placeholder");
}
