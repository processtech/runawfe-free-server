package ru.runa.wfe.chat.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler<EditMessageRequest> {
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    public void handleMessage(EditMessageRequest request, User user) {
        sessionHandler.sendMessage(Delegates.getChatService().editMessage(user, request));
    }

    @Override
    public Class<EditMessageRequest> getRequestType() {
        return EditMessageRequest.class;
    }
}
