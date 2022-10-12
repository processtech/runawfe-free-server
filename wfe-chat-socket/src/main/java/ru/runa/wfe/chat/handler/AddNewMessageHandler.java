package ru.runa.wfe.chat.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@Component
public class AddNewMessageHandler implements ChatSocketMessageHandler<AddMessageRequest> {
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    public void handleMessage(AddMessageRequest request, User user) {
        sessionHandler.sendMessage(Delegates.getChatService().saveMessage(user, request));
    }

    @Override
    public Class<AddMessageRequest> getRequestType() {
        return AddMessageRequest.class;
    }
}
