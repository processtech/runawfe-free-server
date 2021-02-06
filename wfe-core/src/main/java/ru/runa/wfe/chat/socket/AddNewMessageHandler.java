package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.utils.DtoConverters;
import ru.runa.wfe.chat.utils.RecipientCalculator;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@Component
public class AddNewMessageHandler implements ChatSocketMessageHandler<AddMessageRequest> {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private DtoConverters converter;
    @Autowired
    private RecipientCalculator calculator;

    @Transactional
    @Override
    public void handleMessage(Session session, AddMessageRequest dto, User user) throws IOException {
        if (executionLogic.getProcess(user, dto.getProcessId()).isEnded()) {
            return;
        }
        ChatMessage newMessage = converter.convertAddMessageRequestToChatMessage(dto, user.getActor());
        MessageAddedBroadcast messageAddedBroadcast;
        long processId = dto.getProcessId();
        Set<Actor> recipients = calculator.calculateRecipients(user, dto.isPrivate(), dto.getMessage(), processId);
        if (dto.getFiles() != null) {
            ArrayList<ChatMessageFileDto> chatMessageFiles = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : dto.getFiles().entrySet()) {
                ChatMessageFileDto chatMessageFile = new ChatMessageFileDto(entry.getKey(), entry.getValue());
                chatMessageFiles.add(chatMessageFile);
            }
            messageAddedBroadcast = chatLogic.saveMessageAndBindFiles(user, processId, newMessage, recipients, chatMessageFiles);
        } else {
            messageAddedBroadcast = chatLogic.saveMessage(user, processId, newMessage, recipients);
        }
        messageAddedBroadcast.setOld(false);
        messageAddedBroadcast.setCoreUser(messageAddedBroadcast.getAuthor().getId().equals(user.getActor().getId()));
        sessionHandler.sendMessage(calculator.mapToRecipientIds(recipients), messageAddedBroadcast);
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(AddMessageRequest.class);
    }
}
