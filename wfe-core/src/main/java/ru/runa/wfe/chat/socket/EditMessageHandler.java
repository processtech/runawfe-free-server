package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.Set;
import javax.annotation.Resource;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.utils.RecipientCalculator;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler<EditMessageRequest, MessageEditedBroadcast> {

    @Resource(name = "editMessageHandler")
    private EditMessageHandler self;
    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private RecipientCalculator calculator;

    @Transactional
    @Override
    public MessageEditedBroadcast handleMessage(Session session, EditMessageRequest request, User user) throws IOException {
        MessageEditedBroadcast broadcast = null;
        if (self.updateMessage(request, user)) {
            final Set<Actor> recipients = executionLogic.getAllExecutorsByProcessId(user, request.getProcessId(), true);
            broadcast = new MessageEditedBroadcast(request.getEditMessageId(), request.getMessage());
            sessionHandler.sendMessage(calculator.mapToRecipientIds(recipients), broadcast);
        }
        return broadcast;
    }

    @Transactional
    public boolean updateMessage(EditMessageRequest request, User user) {
        ChatMessage newMessage = chatLogic.getMessageById(user, request.getEditMessageId());
        if (newMessage != null) {
            newMessage.setText(request.getMessage());
            chatLogic.updateMessage(user, newMessage);
            return true;
        }

        return false;
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(EditMessageRequest.class);
    }
}
