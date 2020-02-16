package ru.runa.wfe.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.service.decl.ChatServiceLocal;
import ru.runa.wfe.service.decl.ChatServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

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
    public List<Long> getMentionedExecutorIds(User user, Long messageId) {
        return chatLogic.getMentionedExecutorIds(messageId);
    }

    @WebMethod(exclude = true)
    @Override
    public void deleteFile(User user, Long id) {
        chatLogic.deleteFile(user, id);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessageDto saveMessageAndBindFiles(User user, Long processId, ChatMessage message, Set<Executor> mentionedExecutors,
            Boolean isPrivate, ArrayList<ChatMessageFile> files) {
        return chatLogic.saveMessageAndBindFiles(user, processId, message, mentionedExecutors, isPrivate, files);
    }

    @WebMethod(exclude = true)
    @Override
    public void readMessage(User user, Long messageId) {
        chatLogic.readMessage(user.getActor(), messageId);
    }

    @WebMethod(exclude = true)
    @Override
    public Long getLastReadMessage(User user, Long processId) {
        return chatLogic.getLastReadMessage(user.getActor(), processId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<Long> getActiveChatIds(User user) {
        return chatLogic.getActiveChatIds(user.getActor());
    }

    @WebMethod(exclude = true)
    @Override
    public Set<Executor> getAllUsers(User user, Long processId) {
        return chatLogic.getAllUsers(processId, user.getActor());
    }

    @WebMethod(exclude = true)
    @Override
    public List<Long> getNewMessagesCounts(User user, List<Long> processIds, List<Boolean> isMentions) {
        return chatLogic.getNewMessagesCounts(processIds, isMentions, user.getActor());
    }

    @WebMethod(exclude = true)
    @Override
    public void updateChatMessage(User user, ChatMessage message) {
        chatLogic.updateMessage(message);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessageFile> getChatMessageFiles(User user, ChatMessage message) {
        return chatLogic.getMessageFiles(user.getActor(), message);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessageFile getChatMessageFile(User user, Long fileId) {
        return chatLogic.getFile(user.getActor(), fileId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessageFile saveChatMessageFile(User user, ChatMessageFile file) {
        return chatLogic.saveFile(file);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessageDto> getNewChatMessages(User user, Long processId) {
        return chatLogic.getNewMessages(user.getActor(), processId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessage getChatMessage(User user, Long messageId) {
        return chatLogic.getMessage(messageId);
    }

    @WebMethod(exclude = true)
    @Override
    public ChatMessageDto getChatMessageDto(User user, Long messageId) {
        return chatLogic.getMessageDto(messageId);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessageDto> getChatMessages(User user, Long processId, Long firstIndex, int count) {
        return chatLogic.getMessages(user.getActor(), processId, firstIndex, count);
    }

    @WebMethod(exclude = true)
    @Override
    public List<ChatMessageDto> getFirstChatMessages(User user, Long processId, int count) {
        return chatLogic.getFirstMessages(user.getActor(), processId, count);
    }

    @WebMethod(exclude = true)
    @Override
    public void deleteChatMessage(User user, Long messId) {
        chatLogic.deleteMessage(messId);
    }

    @WebMethod(exclude = true)
    @Override
    public Long getNewChatMessagesCount(User user, Long processId) {
        return chatLogic.getNewMessagesCount(user.getActor(), processId);
    }

    @WebMethod(exclude = true)
    @Override
    public Long saveChatMessage(User user, Long processId, ChatMessage message, Set<Executor> mentionedExecutors, Boolean isPrivate) {
        Long id = chatLogic.saveMessage(processId, message, mentionedExecutors, isPrivate);
        chatLogic.sendNotifications(message, mentionedExecutors);
        return id;
    }

}
