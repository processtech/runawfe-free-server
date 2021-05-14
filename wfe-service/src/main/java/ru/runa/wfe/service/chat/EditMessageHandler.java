package ru.runa.wfe.service.chat;

import java.io.IOException;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.socket.ChatSessionHandler;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler<EditMessageRequest> {

    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    @MonitoredWithSpring
    public void handleMessage(EditMessageRequest request, User user) throws IOException {
        sessionHandler.sendMessage(Delegates.getChatService().editMessage(user, request));
    }

    @Override
    public Class<? extends MessageRequest> getRequestType() {
        return EditMessageRequest.class;
    }
}
