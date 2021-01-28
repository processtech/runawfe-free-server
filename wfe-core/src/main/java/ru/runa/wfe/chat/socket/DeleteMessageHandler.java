package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.ChatDeleteMessageDto;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler<ChatDeleteMessageDto> {

    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, ChatDeleteMessageDto dto, User user) throws IOException {
        if (executionLogic.getProcess(user, dto.getProcessId()).isEnded()) {
            return;
        }
        if (!executorLogic.isAdministrator(user)) {
            return;
        }
        chatLogic.deleteMessage(user.getActor(), dto.getMessageId());
    }

    @Override
    public boolean isSupports(Class<? extends ChatDto> messageType) {
        return messageType.equals(ChatDeleteMessageDto.class);
    }
}
