package ru.runa.wfe.chat.logic;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatListener;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatRoom;
import ru.runa.wfe.chat.ChatRoomClassPresentation;
import ru.runa.wfe.chat.CurrentChatMessage;
import ru.runa.wfe.chat.CurrentChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatFileIo;
import ru.runa.wfe.chat.dao.ChatMessageRecipientDao;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.WfChatMessageBroadcast;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.mapper.AddMessageRequestMapper;
import ru.runa.wfe.chat.mapper.ChatMessageFileDetailMapper;
import ru.runa.wfe.chat.mapper.MessageAddedBroadcastFileMapper;
import ru.runa.wfe.chat.mapper.MessageAddedBroadcastMapper;
import ru.runa.wfe.chat.utils.RecipientCalculator;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.Variable;

@Component
@MonitoredWithSpring
public class ChatLogic extends WfCommonLogic {
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private AddMessageRequestMapper messageRequestMapper;
    @Autowired
    private MessageAddedBroadcastMapper messageAddedBroadcastMapper;
    @Autowired
    private MessageAddedBroadcastFileMapper messageFileMapper;
    @Autowired
    private ChatMessageFileDetailMapper fileDetailMapper;
    @Autowired
    private ChatFileIo fileIo;
    @Autowired
    private RecipientCalculator recipientCalculator;
    @Autowired
    private ChatFileDao fileDao;
    @Autowired
    private ChatMessageRecipientDao recipientDao;
    @Autowired(required = false)
    private List<ChatListener> listeners = new ArrayList<>();

    public WfChatMessageBroadcast<MessageAddedBroadcast> saveMessage(User user, AddMessageRequest request) {
        final CurrentChatMessage newMessage = messageRequestMapper.toEntity(request);
        newMessage.setCreateActor(executorDao.getActor(user.getActor().getName()));
        final long processId = request.getProcessId();
        final Set<Actor> recipients = recipientCalculator.calculateRecipients(user, request.getIsPrivate(), request.getText(), processId);

        MessageAddedBroadcast messageAddedBroadcast;
        if (request.getFiles() != null) {
            List<ChatMessageFileDto> chatMessageFiles = new ArrayList<>(request.getFiles().size());
            for (Map.Entry<String, byte[]> entry : request.getFiles().entrySet()) {
                ChatMessageFileDto chatMessageFile = new ChatMessageFileDto(entry.getKey(), entry.getValue());
                chatMessageFiles.add(chatMessageFile);
            }
            messageAddedBroadcast = saveMessageInternal(processId, newMessage, recipients, chatMessageFiles);
        } else {
            messageAddedBroadcast = saveMessageInternal(processId, newMessage, recipients);
        }

        WfChatMessageBroadcast<MessageAddedBroadcast> wfChatMessageBroadcast = new WfChatMessageBroadcast<>(messageAddedBroadcast, recipients);
        listeners.forEach(l -> l.onCreate(wfChatMessageBroadcast));

        return wfChatMessageBroadcast;
    }

    public WfChatMessageBroadcast<MessageEditedBroadcast> editMessage(User user, EditMessageRequest request) {
        final CurrentChatMessage message = chatMessageDao.getNotNull(request.getId());
        message.setText(request.getText());
        if (!message.getCreateActor().equals(user.getActor())) {
            throw new AuthorizationException("Allowed for author only");
        }
        chatMessageDao.update(message);

        WfChatMessageBroadcast<MessageEditedBroadcast> wfChatMessageBroadcast = new WfChatMessageBroadcast<>(
                new MessageEditedBroadcast(request.getProcessId(), message.getId(), message.getText(), user.getName()),
                getRecipientsByMessageId(message.getId())
        );
        listeners.forEach(l -> l.onEdit(wfChatMessageBroadcast));

        return wfChatMessageBroadcast;
    }

    public WfChatMessageBroadcast<MessageDeletedBroadcast> deleteMessage(User user, DeleteMessageRequest request) {
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Allowed for admin only");
        }
        final CurrentChatMessage message = chatMessageDao.getNotNull(request.getId());
        final Set<Actor> recipients = getRecipientsByMessageId(message.getId());
        fileDao.deleteByMessage(message);
        recipientDao.deleteByMessageId(message.getId());
        chatMessageDao.delete(message.getId());

        WfChatMessageBroadcast<MessageDeletedBroadcast> wfChatMessageBroadcast = new WfChatMessageBroadcast<>(
                new MessageDeletedBroadcast(request.getProcessId(), request.getId(), user.getName()), recipients);
        listeners.forEach(l -> l.onDelete(wfChatMessageBroadcast));

        return wfChatMessageBroadcast;
    }

    public ChatMessage getMessageById(User user, Long messageId) {
        return chatMessageDao.get(messageId);
    }

    public List<MessageAddedBroadcast> getMessages(User user, Long processId) {
        List<CurrentChatMessage> messages = chatMessageDao.getMessages(user.getActor(), processId);
        if (!messages.isEmpty()) {
            for (List<CurrentChatMessage> messagesPart : Lists.partition(messages, SystemProperties.getDatabaseParametersCount())) {
                chatMessageDao.readMessages(user.getActor(), messagesPart);
            }
        }
        return messageFileMapper.toDtos(messages);
    }

    public List<MessageAddedBroadcast> getArchivedMessages(User user, Long procesId) {
        return messageFileMapper.toDtos(chatMessageDao.getArchivedMessages(user.getActor(), procesId));
    }

    public Long getNewMessagesCount(User user) {
        return recipientDao.getNewMessagesCount(user.getActor());
    }

    public void deleteMessages(User user, Long processId) {
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Allowed for admin only");
        }
        chatComponentFacade.deleteByProcessId(processId);
    }

    public int getChatRoomsCount(User user, BatchPresentation batchPresentation) {
        batchPresentation.getType().getRestrictions().add(ChatRoomClassPresentation.getExecutorIdRestriction(user.getActor().getId()));
        int count = getPersistentObjectCount(user, batchPresentation, Permission.READ, new SecuredObjectType[]{ SecuredObjectType.PROCESS });
        batchPresentation.getType().getRestrictions().remove(ChatRoomClassPresentation.getExecutorIdRestriction(user.getActor().getId()));
        return count;
    }

    public List<WfChatRoom> getChatRooms(User user, BatchPresentation batchPresentation) {
        batchPresentation.getType().getRestrictions().add(ChatRoomClassPresentation.getExecutorIdRestriction(user.getActor().getId()));
        List<ChatRoom> chatRooms = getPersistentObjects(user, batchPresentation, Permission.READ,
                new SecuredObjectType[]{ SecuredObjectType.PROCESS }, true);
        batchPresentation.getType().getRestrictions().remove(ChatRoomClassPresentation.getExecutorIdRestriction(user.getActor().getId()));
        return toWfChatRooms(chatRooms, batchPresentation.getDynamicFieldsToDisplay(true));
    }

    private MessageAddedBroadcast saveMessageInternal(Long processId, CurrentChatMessage message, Set<Actor> recipients) {
        final ChatMessage savedMessage = chatComponentFacade.save(message, recipients, processId);
        return messageAddedBroadcastMapper.toDto(savedMessage);
    }

    private MessageAddedBroadcast saveMessageInternal(Long processId, CurrentChatMessage message, Set<Actor> recipients, List<ChatMessageFileDto> files) {
        final List<CurrentChatMessageFile> savedFiles = fileIo.save(files);
        final CurrentChatMessage savedMessage = chatComponentFacade.save(message, recipients, savedFiles, processId);
        final MessageAddedBroadcast broadcast = messageAddedBroadcastMapper.toDto(savedMessage);
        broadcast.setFiles(fileDetailMapper.toDtos(savedFiles));
        return broadcast;
    }

    private Set<Actor> getRecipientsByMessageId(Long messageId) {
        return new HashSet<>(recipientDao.getRecipientsByMessageId(messageId));
    }

    @SuppressWarnings("rawtypes")
    private List<WfChatRoom> toWfChatRooms(List<ChatRoom> chatRooms, List<String> variableNamesToInclude) {
        Map<Process, Map<String, Variable>> variables = getVariables(chatRooms, variableNamesToInclude);
        List<WfChatRoom> wfChatRooms = Lists.newArrayListWithExpectedSize(chatRooms.size());
        for (ChatRoom room : chatRooms) {
            CurrentProcess process = room.getProcess();
            WfChatRoom wfChatRoom = new WfChatRoom(process, executionLogic.getProcessErrors(process), room.getNewMessagesCount());
            wfChatRoom.getProcess().addAllVariables(executionLogic.getVariables(variableNamesToInclude, variables, process));
            wfChatRooms.add(wfChatRoom);
        }
        return wfChatRooms;
    }

    @SuppressWarnings("rawtypes")
    private Map<Process, Map<String, Variable>> getVariables(List<ChatRoom> chatRooms, List<String> variableNamesToInclude) {
        List<CurrentProcess> processes = new ArrayList<>(chatRooms.size());
        for (ChatRoom room : chatRooms) {
            processes.add(room.getProcess());
        }
        return variableDao.getVariables(processes, variableNamesToInclude);
    }
}
