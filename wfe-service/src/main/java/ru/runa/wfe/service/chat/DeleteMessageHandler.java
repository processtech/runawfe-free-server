package ru.runa.wfe.service.chat;

import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler<DeleteMessageRequest> {

    @Override
    @MonitoredWithSpring
    public void handleMessage(DeleteMessageRequest request, User user) {
        Delegates.getChatService().deleteMessage(user, request);
    }

    @Override
    public Class<? extends MessageRequest> getRequestType() {
        return DeleteMessageRequest.class;
    }
}
