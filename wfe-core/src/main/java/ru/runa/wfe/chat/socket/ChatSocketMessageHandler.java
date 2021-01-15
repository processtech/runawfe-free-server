package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.user.User;

public interface ChatSocketMessageHandler<T extends ChatDto> {

    void handleMessage(Session session, T dto, User user) throws IOException;

    boolean isSupports(Class<? extends ChatDto> messageType);
}
