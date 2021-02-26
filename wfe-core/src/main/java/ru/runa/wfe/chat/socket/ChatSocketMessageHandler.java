package ru.runa.wfe.chat.socket;

import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.user.User;
import java.io.IOException;

public interface ChatSocketMessageHandler<T extends MessageRequest> {

    void handleMessage(T request, User user) throws IOException;

    Class<? extends MessageRequest> getRequestType();
}
