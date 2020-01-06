package ru.runa.wfe.service.delegate;

import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.service.ChatService;
import ru.runa.wfe.user.Actor;

public class ChatServiceDelegate extends Ejb3Delegate implements ChatService {

    public ChatServiceDelegate() {
        super(ChatService.class);
    }

    private ChatService getChatService() {
        return getService();
    }
	
	@Override
    public boolean sendMessageToEmail(String title, String message, String Emaile) {
        return getChatService().sendMessageToEmail(title, message, Emaile);
    }

    @Override
    public List<Actor> getAllUsersNamesForChat(int chatId) {
        return getChatService().getAllUsersNamesForChat(chatId);
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
    public ChatMessageFile getChatMessageFile(long fileId) {
        return getChatService().getChatMessageFile(fileId);
    }

    @Override
    public ChatMessageFile saveChatMessageFile(ChatMessageFile file) {
        return getChatService().saveChatMessageFile(file);
    }

    @Override
    public List<ChatMessage> getChatMessages(int chatId) {
        return getChatService().getChatMessages(chatId);
    }

    @Override
    public List<ChatMessage> getNewChatMessages(int chatId, Long lastId) {
        return getChatService().getNewChatMessages(chatId, lastId);
    }

    @Override
    public ChatMessage getChatMessage(long messageId) {
        return getChatService().getChatMessage(messageId);
    }

    @Override
    public List<ChatMessage> getChatMessages(int chatId, Long firstId, int count) {
        return getChatService().getChatMessages(chatId, firstId, count);
    }

    @Override
    public List<ChatMessage> getFirstChatMessages(int chatId, int count) {
        return getChatService().getFirstChatMessages(chatId, count);
    }

    @Override
    public void deleteChatMessage(long messId) {
    	getChatService().deleteChatMessage(messId);
    }

    @Override
    public ChatsUserInfo getChatUserInfo(/* long userId, String userName */Actor actor, int chatId) {
        return getChatService().getChatUserInfo(/* userId, userName */actor, chatId);
    }

    @Override
    public long getNewChatMessagesCount(long lastMessageId, int chatId) {
        return getChatService().getNewChatMessagesCount(lastMessageId, chatId);
    }

    @Override
    public void updateChatUserInfo(Actor actor, int chatId, long lastMessageId) {
    	getChatService().updateChatUserInfo(actor, chatId, lastMessageId);
    }

    @Override
    public long getAllChatMessagesCount(int chatId) {
        return getChatService().getAllChatMessagesCount(chatId);
    }

    @Override
    public List<Integer> getAllConnectedChatId(int chatId) {
        return getChatService().getAllConnectedChatId(chatId);
    }

    @Override
    public long saveChatMessage(int chatId, ChatMessage message) {
        return getChatService().saveChatMessage(chatId, message);
    }

}
