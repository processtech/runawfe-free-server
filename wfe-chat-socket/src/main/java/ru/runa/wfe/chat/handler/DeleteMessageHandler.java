package ru.runa.wfe.chat.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler<DeleteMessageRequest> {
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    public void handleMessage(DeleteMessageRequest request, User user) {
        sessionHandler.sendMessage(Delegates.getChatService().deleteMessage(user, request));
    }

    @Override
    public Class<DeleteMessageRequest> getRequestType() {
        return DeleteMessageRequest.class;
    }
}
