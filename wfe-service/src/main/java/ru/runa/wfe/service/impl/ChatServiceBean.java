package ru.runa.wfe.service.impl;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.service.decl.ChatServiceLocal;
import ru.runa.wfe.service.decl.ChatServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.Actor;

@Stateless(name = "ChatServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ChatAPI", serviceName = "ChatWebService")
@SOAPBinding
public class ChatServiceBean implements ChatServiceLocal, ChatServiceRemote {

    @Autowired
    private ChatLogic chatLogic;

    @WebMethod(exclude = true)
    @Override
    public boolean chatSendMessageToEmail(String title, String message, String Emaile) {
        return chatLogic.sendMessageToEmail(title, message, Emaile);
    }

    @WebMethod(exclude = true)
    @Override
    public List<Actor> getAllUsersNamesForChat(int chatId) {
        return chatLogic.getAllUsersNames(chatId);
    }

    @WebMethod(exclude = true)
    @Override
    public void updateChatMessage(ChatMessage message) {
        chatLogic.updateMessage(message);
    }

    @WebMethod(exclude = true)
    @Override
    public boolean canEditChatMessage(Actor user) {
        return chatLogic.canEditMessage(user);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessageFile> getChatMessageFiles(ChatMessage message) {
        return chatLogic.getMessageFiles(message);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessageFile getChatMessageFile(long fileId) {
        return chatLogic.getFile(fileId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessageFile saveChatMessageFile(ChatMessageFile file) {
        return chatLogic.saveFile(file);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessage> getChatMessages(int chatId) {
        return chatLogic.getMessages(chatId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessage> getChatNewMessages(int chatId, Long lastId) {
        return chatLogic.getNewMessages(chatId, lastId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessage getChatMessage(long messageId) {
        return chatLogic.getMessage(messageId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessage> getChatMessages(int chatId, Long firstIndex, int count) {
        return chatLogic.getMessages(chatId, firstIndex, count);
    }

    @WebMethod(exclude = true)
    @Override
    public long getChatAllMessagesCount(int chatId) {
        return chatLogic.getAllMessagesCount(chatId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessage> getChatFirstMessages(int chatId, int count) {
        return chatLogic.getFirstMessages(chatId, count);
    }

    @WebMethod(exclude = true)
    @Override
    public void deleteChatMessage(long messId) {
        chatLogic.deleteMessage(messId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<Integer> getChatAllConnectedChatId(int chatId) {
        return chatLogic.getAllConnectedChatId(chatId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatsUserInfo getChatUserInfo(Actor actor, int chatId) {
        return chatLogic.getUserInfo(actor, chatId);
    }

    @WebMethod(exclude = true)
    @Override
    public long getChatNewMessagesCount(long lastMessageId, int chatId) {
        return chatLogic.getNewMessagesCount(lastMessageId, chatId);
    }

    @WebMethod(exclude = true)
    @Override
    public void updateChatUserInfo(Actor actor, int chatId, long lastMessageId) {
        chatLogic.updateUserInfo(actor, chatId, lastMessageId);
    }

    @WebMethod(exclude = true)
    @Override
    public long setChatMessage(int chatId, ChatMessage message) {
        return chatLogic.setMessage(chatId, message);
    }
}
