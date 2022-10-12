package ru.runa.wfe.chat.handler;

import java.io.IOException;
import ru.runa.wfe.chat.dto.ClientMessage;
import ru.runa.wfe.user.User;

public interface ChatSocketMessageHandler<T extends ClientMessage> {

    void handleMessage(T request, User user) throws IOException;

    Class<T> getRequestType();
}
