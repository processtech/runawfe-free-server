package ru.runa.wfe.chat.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.utils.DtoConverters;
import ru.runa.wfe.chat.utils.RecipientCalculator;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AddNewMessageHandler implements ChatSocketMessageHandler<AddMessageRequest> {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private DtoConverters converter;
    @Autowired
    private RecipientCalculator calculator;

    @Override
    public void handleMessage(AddMessageRequest request, User user) throws IOException {
        final ChatMessage newMessage = converter.convertAddMessageRequestToChatMessage(request, user.getActor());
        final long processId = request.getProcessId();
        final Set<Actor> recipients = calculator.calculateRecipients(user, request.isPrivate(), request.getMessage(), processId);

        MessageAddedBroadcast messageAddedBroadcast;
        if (request.getFiles() != null) {
            List<ChatMessageFileDto> chatMessageFiles = new ArrayList<>(request.getFiles().size());
            for (Map.Entry<String, byte[]> entry : request.getFiles().entrySet()) {
                ChatMessageFileDto chatMessageFile = new ChatMessageFileDto(entry.getKey(), entry.getValue());
                chatMessageFiles.add(chatMessageFile);
            }
            messageAddedBroadcast = chatLogic.saveMessage(user, processId, newMessage, recipients, chatMessageFiles);
        } else {
            messageAddedBroadcast = chatLogic.saveMessage(user, processId, newMessage, recipients);
        }

        sessionHandler.sendMessage(calculator.mapToRecipientIds(recipients), messageAddedBroadcast);
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(AddMessageRequest.class);
    }
}
