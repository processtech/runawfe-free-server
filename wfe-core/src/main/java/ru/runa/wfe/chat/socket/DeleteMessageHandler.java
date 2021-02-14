package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.Set;
import javax.annotation.Resource;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.utils.RecipientCalculator;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler<DeleteMessageRequest> {

    @Resource(name = "deleteMessageHandler")
    private DeleteMessageHandler self;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private RecipientCalculator calculator;

    @Transactional
    @Override
    public void handleMessage(Session session, DeleteMessageRequest request, User user) throws IOException {
        self.deleteMessage(request, user);
        final Set<Actor> recipients = executionLogic.getAllExecutorsByProcessId(user, request.getProcessId(), true);
        sessionHandler.sendMessage(calculator.mapToRecipientIds(recipients), new MessageDeletedBroadcast(request.getMessageId()));
    }

    @Transactional
    public void deleteMessage(DeleteMessageRequest dto, User user) {
        chatLogic.deleteMessage(user, dto.getMessageId());
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(DeleteMessageRequest.class);
    }
}
