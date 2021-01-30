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
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.ChatNewMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.chat.utils.ChatNewMessageDtoToChatMessageConverter;
import ru.runa.wfe.chat.utils.MentionedExecutorsExtractor;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Component
public class AddNewMessageHandler implements ChatSocketMessageHandler<ChatNewMessageDto> {

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
    public void handleMessage(Session session, ChatNewMessageDto dto, User user) throws IOException {
        if (executionLogic.getProcess(user, dto.getProcessId()).isEnded()) {
            return;
        }
        boolean isPrivate = dto.isPrivate();
        Actor actor = user.getActor();
        ChatMessage newMessage = converter.convert(dto, actor);
        Set<Executor> mentionedExecutors = extractor.extractMentionedExecutors(dto.getPrivateNames(), newMessage, user);
        Collection<Long> recipientIds = extractor.extractRecipientIds(mentionedExecutors, isPrivate);
        ChatMessageDto chatMessageDto;
        long processId = dto.getProcessId();
        if (dto.getFiles() != null) {
            ArrayList<ChatMessageFileDto> chatMessageFiles = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : dto.getFiles().entrySet()) {
                ChatMessageFileDto chatMessageFile = new ChatMessageFileDto(entry.getKey(), entry.getValue());
                chatMessageFiles.add(chatMessageFile);
            }
            chatMessageDto = chatLogic.saveMessageAndBindFiles(actor, processId, newMessage, mentionedExecutors,
                    isPrivate, chatMessageFiles);
        } else {
            Long newMessId = chatLogic.saveMessage(actor, processId, newMessage, mentionedExecutors, isPrivate);
            newMessage.setId(newMessId);
            chatMessageDto = new ChatMessageDto(newMessage);
        }
        chatMessageDto.setOld(false);
        sessionHandler.sendMessage(recipientIds, chatMessageDto);
    }

    @Override
    public boolean isSupports(Class<? extends ChatDto> messageType) {
        return messageType.equals(ChatNewMessageDto.class);
    }
}
