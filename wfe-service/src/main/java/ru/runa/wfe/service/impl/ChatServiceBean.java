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
    public boolean sendMessageToEmail(String title, String message, String Emaile) {
        return chatLogic.sendMessageToEmail(title, message, Emaile);
    }

    @WebMethod(exclude = true)
    @Override
    public List<Actor> getAllUsersNamesForChat(Long processId) {
        return chatLogic.getAllUsersNames(processId);
    }

    @WebMethod(exclude = true)
    @Override
    public void updateChatMessage(ChatMessage message) {
        chatLogic.updateMessage(message);
    }

    @WebMethod(exclude = true)
    @Override
    public boolean canEditMessage(Actor user) {
        return chatLogic.canEditMessage(user);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessageFile> getChatMessageFiles(ChatMessage message) {
        return chatLogic.getMessageFiles(message);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessageFile getChatMessageFile(Long fileId) {
        return chatLogic.getFile(fileId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessageFile saveChatMessageFile(ChatMessageFile file) {
        return chatLogic.saveFile(file);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessage> getChatMessages(Long processId) {
        return chatLogic.getMessages(processId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessage> getNewChatMessages(Long processId, Long lastId) {
        return chatLogic.getNewMessages(processId, lastId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessage getChatMessage(Long messageId) {
        return chatLogic.getMessage(messageId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessage> getChatMessages(Long processId, Long firstIndex, int count) {
        return chatLogic.getMessages(processId, firstIndex, count);
    }

    @WebMethod(exclude = true)
    @Override
    public long getAllChatMessagesCount(Long processId) {
        return chatLogic.getAllMessagesCount(processId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessage> getFirstChatMessages(Long processId, int count) {
        return chatLogic.getFirstMessages(processId, count);
    }

    @WebMethod(exclude = true)
    @Override
    public void deleteChatMessage(Long messId) {
        chatLogic.deleteMessage(messId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatsUserInfo getChatUserInfo(Actor actor, Long processId) {
        return chatLogic.getUserInfo(actor, processId);
    }

    @WebMethod(exclude = true)
    @Override
    public long getNewChatMessagesCount(Long lastMessageId, Long processId) {
        return chatLogic.getNewMessagesCount(lastMessageId, processId);
    }

    @WebMethod(exclude = true)
    @Override
    public void updateChatUserInfo(Actor actor, Long processId, Long lastMessageId) {
        chatLogic.updateUserInfo(actor, processId, lastMessageId);
    }

    @WebMethod(exclude = true)
    @Override
    public long saveChatMessage(Long processId, ChatMessage message) {
        return chatLogic.setMessage(processId, message);
    }
}
