package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.Set;
import javax.annotation.Resource;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.utils.RecipientCalculator;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler<EditMessageRequest> {

    @Resource(name = "editMessageHandler")
    private EditMessageHandler self;
    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private RecipientCalculator calculator;

    @Override
    public void handleMessage(Session session, EditMessageRequest request, User user) throws IOException {
        final Set<Actor> recipients = calculator.calculateRecipients(user, false, request.getMessage(), request.getProcessId());
        if (self.updateMessage(request, user)) {
            sessionHandler.sendMessage(calculator.mapToRecipientIds(recipients), new MessageEditedBroadcast(request.getEditMessageId(),
                    request.getMessage()));
        }
    }

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
