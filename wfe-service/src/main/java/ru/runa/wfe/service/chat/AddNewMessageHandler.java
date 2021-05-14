package ru.runa.wfe.service.chat;

import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.socket.ChatSessionHandler;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@Component
public class AddNewMessageHandler implements ChatSocketMessageHandler<AddMessageRequest> {

    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    @MonitoredWithSpring
    public void handleMessage(AddMessageRequest request, User user) {
        sessionHandler.sendMessage(Delegates.getChatService().saveMessage(user, request));
    }

    @Override
    public Class<? extends MessageRequest> getRequestType() {
        return AddMessageRequest.class;
    }
}
