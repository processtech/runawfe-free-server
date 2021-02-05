package ru.runa.wfe.service.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.service.ChatService;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

public class ChatServiceDelegate extends Ejb3Delegate implements ChatService {

    public ChatServiceDelegate() {
        super(ChatService.class);
    }

    private ChatService getChatService() {
        return getService();
    }

    @Override
    public List<Long> getMentionedExecutorIds(User user, Long messageId) {
        return getChatService().getMentionedExecutorIds(user, messageId);
    }

    @Override
    public void deleteFile(User user, Long id) {
        getChatService().deleteFile(user, id);
    }

    @Override
    public ChatMessage saveMessageAndBindFiles(User user, Long processId, ChatMessage message, Set<Actor> recipients,
                                               ArrayList<ChatMessageFile> files) {
        return getChatService().saveMessageAndBindFiles(user, processId, message, recipients, files);
    }

    @Override
    public void readMessage(User user, Long messageId) {
        getChatService().readMessage(user, messageId);
    }

    @Override
    public Long getLastReadMessage(User user, Long processId) {
        return getChatService().getLastReadMessage(user, processId);
    }

    @Override
    public List<Long> getActiveChatIds(User user) {
        return getChatService().getActiveChatIds(user);
    }

    @Override
    public List<Long> getNewMessagesCounts(User user, List<Long> processIds) {
        return getChatService().getNewMessagesCounts(user, processIds);
    }
	
    @Override
    public void updateChatMessage(User user, ChatMessage message) {
        getChatService().updateChatMessage(user, message);
    }

    @Override
    public List<ChatMessageFile> getChatMessageFiles(User user, ChatMessage message) {
        return getChatService().getChatMessageFiles(user, message);
    }

    @Override
    public ChatMessageFile getChatMessageFile(User user, Long fileId) {
        return getChatService().getChatMessageFile(user, fileId);
    }

    @Override
    public ChatMessageFile saveChatMessageFile(User user, ChatMessageFile file) {
        return getChatService().saveChatMessageFile(user, file);
    }

    @Override
    public List<MessageAddedBroadcast> getNewChatMessages(User user, Long processId) {
        return getChatService().getNewChatMessages(user, processId);
    }

    @Override
    public ChatMessage getChatMessage(User user, Long messageId) {
        return getChatService().getChatMessage(user, messageId);
    }

    @Override
    public List<MessageAddedBroadcast> getChatMessages(User user, Long processId, Long firstId, int count) {
        return getChatService().getChatMessages(user, processId, firstId, count);
    }

    @Override
    public void deleteChatMessage(User user, Long messId) {
        getChatService().deleteChatMessage(user, messId);
    }

    @Override
    public Long getNewChatMessagesCount(User user, Long processId) {
        return getChatService().getNewChatMessagesCount(user, processId);
    }

    @Override
    public Long saveChatMessage(User user, Long processId, ChatMessage message, Set<Actor> recipients) {
        return getChatService().saveChatMessage(user, processId, message, recipients);
    }

    @Override
    public Long getLastMessage(User user, Long processId) {
        return getChatService().getLastMessage(user, processId);
    }

}
