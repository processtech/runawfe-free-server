package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.dto.ChatEditMessageDto;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.User;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler<ChatEditMessageDto> {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ExecutionLogic executionLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, ChatEditMessageDto dto, User user) throws IOException {
        if (executionLogic.getProcess(user, (Long) session.getUserProperties().get("processId")).isEnded()) {
            return;
        }
        ChatMessage newMessage = chatLogic.getMessage(user.getActor(), dto.getEditMessageId());
        if ((newMessage != null) && (newMessage.getCreateActor().equals(user.getActor()))) {
            newMessage.setText(dto.getMessage());
            chatLogic.updateMessage(user.getActor(), newMessage);

            ChatMessageDto messageDto = new ChatMessageDto();
            messageDto.setMessage(newMessage);
            sessionHandler.sendToChats(messageDto, dto.getProcessId());
        }
    }

    @Override
    public boolean isSupports(Class<? extends ChatDto> messageType) {
        return messageType.equals(ChatEditMessageDto.class);
    }
}
