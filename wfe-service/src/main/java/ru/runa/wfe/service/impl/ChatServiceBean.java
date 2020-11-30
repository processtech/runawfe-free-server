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

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<Long> getMentionedExecutorIds(@WebParam(name = "user") @NonNull User user, @WebParam(name = "message") Long messageId) {
        return chatLogic.getMentionedExecutorIds(messageId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void deleteFile(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        chatLogic.deleteFile(user, id);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessageDto saveMessageAndBindFiles(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "message") ChatMessage message, @WebParam(name = "mentionedExecutors") Set<Executor> mentionedExecutors,
            Boolean isPrivate, ArrayList<ChatMessageFile> files) {
        return chatLogic.saveMessageAndBindFiles(user.getActor(), processId, message, mentionedExecutors, isPrivate, files);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void readMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "messageId") Long messageId) {
        chatLogic.readMessage(user.getActor(), messageId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long getLastReadMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getLastReadMessage(user.getActor(), processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long getLastMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getLastMessage(user.getActor(), processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<Long> getActiveChatIds(@WebParam(name = "user") @NonNull User user) {
        return chatLogic.getActiveChatIds(user.getActor());
    }

    @WebMethod(exclude = false)
    @WebResult(name = "result")
    @Override
    public Set<Executor> getAllUsers(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getAllUsers(processId, user.getActor());
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<Long> getNewMessagesCounts(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processIds") List<Long> processIds,
            @WebParam(name = "isMentions") List<Boolean> isMentions) {
        return chatLogic.getNewMessagesCounts(processIds, isMentions, user.getActor());
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void updateChatMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "message") ChatMessage message) {
        chatLogic.updateMessage(user.getActor(), message);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<ChatMessageFile> getChatMessageFiles(@WebParam(name = "user") @NonNull User user, @WebParam(name = "message") ChatMessage message) {
        return chatLogic.getMessageFiles(user.getActor(), message);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessageFile getChatMessageFile(@WebParam(name = "user") @NonNull User user, @WebParam(name = "fileId") Long fileId) {
        return chatLogic.getFile(user.getActor(), fileId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessageFile saveChatMessageFile(@WebParam(name = "user") @NonNull User user, @WebParam(name = "file") ChatMessageFile file) {
        return chatLogic.saveFile(file);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<ChatMessageDto> getNewChatMessages(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getNewMessages(user.getActor(), processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessage getChatMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "messageId") Long messageId) {
        return chatLogic.getMessage(user.getActor(), messageId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public ChatMessageDto getChatMessageDto(@WebParam(name = "user") @NonNull User user, @WebParam(name = "messageId") Long messageId) {
        return chatLogic.getMessageDto(messageId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<ChatMessageDto> getChatMessages(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "firstIndex") Long firstIndex, @WebParam(name = "count") int count) {
        return chatLogic.getMessages(user.getActor(), processId, firstIndex, count);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public List<ChatMessageDto> getFirstChatMessages(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "count") int count) {
        return chatLogic.getFirstMessages(user.getActor(), processId, count);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public void deleteChatMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "messId") Long messId) {
        chatLogic.deleteMessage(user.getActor(), messId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long getNewChatMessagesCount(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId) {
        return chatLogic.getNewMessagesCount(user.getActor(), processId);
    }

    @WebMethod(exclude = false)
    @Override
    @WebResult(name = "result")
    public Long saveChatMessage(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "message") ChatMessage message, @WebParam(name = "mentionedExecutors") Set<Executor> mentionedExecutors,
            @WebParam(name = "isPrivate") Boolean isPrivate) {
        return chatLogic.saveMessage(user.getActor(), processId, message, mentionedExecutors, isPrivate);
    }

}
