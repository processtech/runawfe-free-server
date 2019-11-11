package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFiles;
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
    public boolean chatSendMessageToEmail(String title, String message, String Emaile) {
        return getChatService().chatSendMessageToEmail(title, message, Emaile);
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
    public boolean canEditChatMessage(Actor user) {
        return getChatService().canEditChatMessage(user);
    }

    @Override
    public List<ChatMessageFiles> getChatMessageFiles(ChatMessage message) {
        return getChatService().getChatMessageFiles(message);
    }

    @Override
    public ChatMessageFiles getChatMessageFile(long fileId) {
        return getChatService().getChatMessageFile(fileId);
    }

    @Override
    public ChatMessageFiles saveChatMessageFile(ChatMessageFiles file) {
        return getChatService().saveChatMessageFile(file);
    }

    @Override
    public List<ChatMessage> getChatMessages(int chatId) {
        return getChatService().getChatMessages(chatId);
    }

    @Override
    public List<ChatMessage> getChatNewMessages(int chatId, Long lastId) {
        return getChatService().getChatNewMessages(chatId, lastId);
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
    public List<ChatMessage> getChatFirstMessages(int chatId, int count) {
        return getChatService().getChatFirstMessages(chatId, count);
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
    public long getChatNewMessagesCount(long lastMessageId, int chatId) {
        return getChatService().getChatNewMessagesCount(lastMessageId, chatId);
    }

    @Override
    public void updateChatUserInfo(Actor actor, int chatId, long lastMessageId) {
    	getChatService().updateChatUserInfo(actor, chatId, lastMessageId);
    }

    @Override
    public long getChatAllMessagesCount(int chatId) {
        return getChatService().getChatAllMessagesCount(chatId);
    }

    @Override
    public List<Integer> getChatAllConnectedChatId(int chatId) {
        return getChatService().getChatAllConnectedChatId(chatId);
    }

    @Override
    public long setChatMessage(int chatId, ChatMessage message) {
        return getChatService().setChatMessage(chatId, message);
    }

}
