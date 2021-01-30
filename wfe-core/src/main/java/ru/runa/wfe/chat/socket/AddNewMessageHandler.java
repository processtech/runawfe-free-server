package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.broadcast.AddedMessageBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.utils.ChatNewMessageDtoToChatMessageConverter;
import ru.runa.wfe.chat.utils.MentionedExecutorsExtractor;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
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
    private ChatNewMessageDtoToChatMessageConverter converter;
    @Autowired
    private MentionedExecutorsExtractor extractor;

    @Transactional
    @Override
    public void handleMessage(Session session, AddMessageRequest dto, User user) throws IOException {
        if (executionLogic.getProcess(user, dto.getProcessId()).isEnded()) {
            return;
        }
        boolean isPrivate = dto.isPrivate();
        Actor actor = user.getActor();
        ChatMessage newMessage = converter.convert(dto, actor);
        Set<Executor> mentionedExecutors = extractor.extractMentionedExecutors(dto.getPrivateNames(), newMessage, user);
        Collection<Long> recipientIds = extractor.extractRecipientIds(mentionedExecutors, isPrivate);
        AddedMessageBroadcast broadcastDto;
        long processId = dto.getProcessId();
        if (dto.getFiles() != null) {
            ArrayList<ChatMessageFile> chatMessageFiles = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : dto.getFiles().entrySet()) {
                ChatMessageFile chatMessageFile = new ChatMessageFile(entry.getKey(), entry.getValue());
                chatMessageFiles.add(chatMessageFile);
            }
            broadcastDto = chatLogic.saveMessageAndBindFiles(actor, processId, newMessage, mentionedExecutors,
                    isPrivate, chatMessageFiles);
        } else {
            Long newMessId = chatLogic.saveMessage(actor, processId, newMessage, mentionedExecutors, isPrivate);
            newMessage.setId(newMessId);
            broadcastDto = new AddedMessageBroadcast(newMessage);
        }
        broadcastDto.setOld(false);
        broadcastDto.setCoreUser(broadcastDto.getAuthor().getId().equals(user.getActor().getId()));
        sessionHandler.sendMessage(recipientIds, broadcastDto);
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(AddMessageRequest.class);
    }
}
