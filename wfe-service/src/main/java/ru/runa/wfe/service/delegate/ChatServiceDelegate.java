package ru.runa.wfe.service.delegate;

import java.util.List;
import java.util.Set;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.service.ChatService;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

public class ChatServiceDelegate extends Ejb3Delegate implements ChatService {

    public ChatServiceDelegate() {
        super(ChatService.class);
    }

    private ChatService getChatService() {
        return getService();
    }

    @Override
    public List<Long> getActiveChatIds(Actor user) {
        return getChatService().getActiveChatIds(user);
    }

    @Override
    public Set<Executor> getAllUsers(Long processId, Actor user) {
        return getChatService().getAllUsers(processId, user);
    }

    @Override
    public List<Long> getNewMessagesCounts(List<Long> chatsIds, List<Boolean> isMentions, Actor user) {
        return getChatService().getNewMessagesCounts(chatsIds, isMentions, user);
    }
	
	@Override
    public boolean sendMessageToEmail(String title, String message, String Emaile) {
        return getChatService().sendMessageToEmail(title, message, Emaile);
    }

    @Override
    public void updateChatMessage(ChatMessage message) {
    	getChatService().updateChatMessage(message);
    }

    @Override
    public boolean canEditMessage(Actor user) {
        return getChatService().canEditMessage(user);
    }

    @Override
    public List<ChatMessageFile> getChatMessageFiles(ChatMessage message) {
        return getChatService().getChatMessageFiles(message);
    }

    @Override
    public ChatMessageFile getChatMessageFile(Long fileId) {
        return getChatService().getChatMessageFile(fileId);
    }

    @Override
    public ChatMessageFile saveChatMessageFile(ChatMessageFile file) {
        return getChatService().saveChatMessageFile(file);
    }

    @Override
    public List<ChatMessage> getChatMessages(Long processId) {
        return getChatService().getChatMessages(processId);
    }

    @Override
    public List<ChatMessage> getNewChatMessages(Long processId, Long lastId) {
        return getChatService().getNewChatMessages(processId, lastId);
    }

    @Override
    public ChatMessage getChatMessage(Long messageId) {
        return getChatService().getChatMessage(messageId);
    }

    @Override
    public List<ChatMessage> getChatMessages(Long processId, Long firstId, int count) {
        return getChatService().getChatMessages(processId, firstId, count);
    }

    @Override
    public List<ChatMessage> getFirstChatMessages(Long processId, int count) {
        return getChatService().getFirstChatMessages(processId, count);
    }

    @Override
    public void deleteChatMessage(Long messId) {
    	getChatService().deleteChatMessage(messId);
    }

    @Override
    public ChatsUserInfo getChatUserInfo(Actor actor, Long processId) {
        return getChatService().getChatUserInfo(actor, processId);
    }

    @Override
    public long getNewChatMessagesCount(Long lastMessageId, Long processId) {
        return getChatService().getNewChatMessagesCount(lastMessageId, processId);
    }

    @Override
    public void updateChatUserInfo(Actor actor, Long processId, Long lastMessageId) {
        getChatService().updateChatUserInfo(actor, processId, lastMessageId);
    }

    @Override
    public long getAllChatMessagesCount(Long processId) {
        return getChatService().getAllChatMessagesCount(processId);
    }

    @Override
    public long saveChatMessage(Long processId, ChatMessage message) {
        return getChatService().saveChatMessage(processId, message);
    }


}
