package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.List;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler<DeleteMessageRequest> {

    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    @MonitoredWithSpring
    public void handleMessage(DeleteMessageRequest request, User user) throws IOException {
        List<Long> recipientIds = chatLogic.getRecipientIdsByMessageId(user, request.getMessageId());
        chatLogic.deleteMessage(user, request.getMessageId());
        sessionHandler.sendMessage(recipientIds,
                new MessageDeletedBroadcast(request.getProcessId(), request.getMessageId(), user.getName()));
    }

    @Override
    public Class<? extends MessageRequest> getRequestType() {
        return DeleteMessageRequest.class;
    }
}
