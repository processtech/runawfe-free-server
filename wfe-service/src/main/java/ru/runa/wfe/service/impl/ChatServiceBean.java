package ru.runa.wfe.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
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
@Interceptors({EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class})
@WebService(name = "ChatAPI", serviceName = "ChatWebService")
@SOAPBinding
public class ChatServiceBean implements ChatServiceLocal, ChatServiceRemote {

    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ChatFileLogic chatFileLogic;

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<Long> getMentionedExecutorIds(@WebParam(name = "user") @NonNull User user, @WebParam(name = "message") Long messageId) {
        return chatLogic.getMentionedExecutorIds(user, messageId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void deleteFile(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        chatFileLogic.deleteById(user, id);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessage saveMessageAndBindFiles(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "message") ChatMessage message, @WebParam(name = "mentionedExecutors") Set<Executor> mentionedExecutors,
            Boolean isPrivate, ArrayList<ChatMessageFile> files) {
        return chatLogic.saveMessageAndBindFiles(processId, message, mentionedExecutors, isPrivate, files);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void readMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "messageId") Long messageId) {
        chatLogic.readMessage(user, messageId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long getLastReadMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getLastReadMessage(user, processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long getLastMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getLastMessage(user, processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<Long> getActiveChatIds(@WebParam(name = "user") @NonNull User user) {
        return chatLogic.getActiveChatIds(user);
    }

    @WebMethod(exclude = false)
    @WebResult(name = "result")
    @Override
    public Set<Executor> getAllUsers(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getAllUsers(user, processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<Long> getNewMessagesCounts(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "processIds") List<Long> processIds) {
        return chatLogic.getNewMessagesCounts(user, processIds);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void updateChatMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "message") ChatMessage message) {
        chatLogic.updateMessage(user, message);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<ChatMessageFileDto> getChatMessageFiles(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "message") ChatMessage message) {
        return chatFileLogic.getDtoByMessage(user, message);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessageFileDto getChatMessageFile(@WebParam(name = "user") @NonNull User user, @WebParam(name = "fileId") Long fileId) {
        return chatFileLogic.getById(user, fileId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessageFileDto saveChatMessageFile(@WebParam(name = "user") @NonNull User user, @WebParam(name = "file") ChatMessageFileDto file) {
        return chatFileLogic.save(user, file);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<ChatMessageDto> getNewChatMessages(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getNewMessages(user, processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessage getChatMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "messageId") Long messageId) {
        return chatLogic.getMessageById(user, messageId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<MessageAddedBroadcast> getChatMessages(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "firstIndex") Long firstIndex, @WebParam(name = "count") int count) {
        return chatLogic.getMessages(user.getActor(), processId, firstIndex, count);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void deleteChatMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "messId") Long messId) {
        chatLogic.deleteMessage(user, messId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long getNewChatMessagesCount(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getNewMessagesCount(user, processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long saveChatMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "message") ChatMessage message, @WebParam(name = "mentionedExecutors") Set<Executor> mentionedExecutors,
            @WebParam(name = "isPrivate") Boolean isPrivate) {
        return chatLogic.saveMessage(user, processId, message, mentionedExecutors, isPrivate).getMessage().getId();
    }

}
