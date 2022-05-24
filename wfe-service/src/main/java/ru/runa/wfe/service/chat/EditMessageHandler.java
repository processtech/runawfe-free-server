package ru.runa.wfe.service.chat;

import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler<EditMessageRequest> {

    @Override
    @MonitoredWithSpring
    public void handleMessage(EditMessageRequest request, User user) {
        Delegates.getChatService().editMessage(user, request);
    }

    @Override
    public Class<? extends MessageRequest> getRequestType() {
        return EditMessageRequest.class;
    }
}
