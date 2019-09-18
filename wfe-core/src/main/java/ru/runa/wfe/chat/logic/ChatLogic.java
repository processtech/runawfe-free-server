package ru.runa.wfe.chat.logic;

import java.util.List;

import org.springframework.stereotype.Component;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFiles;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.user.Actor;

@Component
public class ChatLogic extends WfCommonLogic {
    public ChatsUserInfo getUserInfo(Actor actor, int chatId) {
        return chatDao.getUserInfo(actor, chatId);
    }

    public long getNewMessagesCount(long lastMessageId, int chatId) {
        return chatDao.getNewMessagesCount(lastMessageId, chatId);
    }

    public void updateUserInfo(Actor actor, int chatId, long lastMessageId) {
        chatDao.updateUserInfo(actor, chatId, lastMessageId);
    }

    public List<ChatMessage> getMessages(int chatId) {
        return chatDao.getAll(chatId);
    }

    public ChatMessage getMessage(long messageId) {
        return chatDao.getMessage(messageId);
    }

    public List<ChatMessage> getMessages(int chatId, int firstId, int count) {
        return chatDao.getMessages(chatId, firstId, count);
    }

    public List<ChatMessage> getFirstMessages(int chatId, int count) {
        return chatDao.getFirstMessages(chatId, count);
    }

    public long setMessage(int chatId, ChatMessage message) {
        return chatDao.save(message);
    }

    public long getAllMessagesCount(int chatId) {
        return chatDao.getMessagesCount(chatId);
    }

    public void deleteMessage(long messId) {
        chatDao.deleteMessage(messId);
    }

    public List<Integer> getAllConnectedChatId(int chatId) {
        return chatDao.getAllConnectedChatId(chatId);
    }

    public List<ChatMessageFiles> getMessageFiles(ChatMessage message) {
        return chatDao.getMessageFiles(message);
    }

    public ChatMessageFiles saveFile(ChatMessageFiles file) {
        return chatDao.saveFile(file);
    }

    public ChatMessageFiles getFile(long fileId) {
        return chatDao.getFile(fileId);
    }

    public void updateMessage(ChatMessage message) {
        chatDao.updateMessage(message);
    }

    public boolean canEditMessage(Actor user) {
        return true;
    }

    public List<Actor> getAllUsersNames(int chatId) {
        return chatDao.getAllUsersNames(chatId);
    }

}
