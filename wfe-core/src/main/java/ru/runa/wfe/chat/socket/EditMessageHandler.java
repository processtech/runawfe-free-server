package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatEditMessageDto;
import ru.runa.wfe.chat.dto.ChatEditMessageResponseDto;
import ru.runa.wfe.chat.dto.ChatNewMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.User;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ExecutionLogic executionLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, String objectMessage, User user) throws IOException {
        if (executionLogic.getProcess(user, (Long) session.getUserProperties().get("processId")).isEnded()) {
            return;
        }
        ChatEditMessageDto message = (ChatEditMessageDto) ChatNewMessageDto.load(objectMessage, ChatEditMessageDto.class);
        ChatMessage newMessage = chatLogic.getMessage(user.getActor(), message.getEditMessageId());
        if ((newMessage != null) && (newMessage.getCreateActor().equals(user.getActor()))) {
            newMessage.setText(message.getMessage());
            chatLogic.updateMessage(user.getActor(), newMessage);

            ChatEditMessageResponseDto responseMessage = new ChatEditMessageResponseDto();
            responseMessage.setMessageType("editMessage");
            responseMessage.setMessageId(newMessage.getId());
            responseMessage.setMessageText(newMessage.getText());
            sessionHandler.sendToChats(responseMessage, message.getProcessId());// processId
        }
    }

    @Override
    public boolean checkType(String messageType) {
        return messageType.equals("editMessage");
    }

}
