package ru.runa.wfe.service.delegate;

import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.WfChatMessageBroadcast;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.ChatService;
import ru.runa.wfe.user.User;

public class ChatServiceDelegate extends Ejb3Delegate implements ChatService {

    public ChatServiceDelegate() {
        super(ChatService.class);
    }

    private ChatService getChatService() {
        return getService();
    }

    @Override
    public WfChatMessageBroadcast<MessageAddedBroadcast> saveMessage(User user, AddMessageRequest request) {
        try {
            return getChatService().saveMessage(user, request);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfChatMessageBroadcast<MessageEditedBroadcast> editMessage(User user, EditMessageRequest request) {
        try {
            return getChatService().editMessage(user, request);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfChatMessageBroadcast<MessageDeletedBroadcast> deleteMessage(User user, DeleteMessageRequest request) {
        try {
            return getChatService().deleteMessage(user, request);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public ChatMessage getMessage(User user, Long id) {
        try {
            return getChatService().getMessage(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<MessageAddedBroadcast> getMessages(User user, Long processId) {
        try {
            return getChatService().getMessages(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<MessageAddedBroadcast> getArchivedMessages(User user, Long processId) {
        try {
            return getChatService().getArchivedMessages(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Long getNewMessagesCount(User user) {
        try {
            return getChatService().getNewMessagesCount(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getChatRoomsCount(User user, BatchPresentation batchPresentation) {
        try {
            return getChatService().getChatRoomsCount(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfChatRoom> getChatRooms(User user, BatchPresentation batchPresentation) {
        try {
            return getChatService().getChatRooms(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public ChatMessageFileDto getChatMessageFile(User user, Long fileId) {
        try {
            return getChatService().getChatMessageFile(user, fileId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public ChatMessageFileDto getArchiveChatMessageFile(User user, Long fileId) {
        try {
            return getChatService().getArchiveChatMessageFile(user, fileId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void deleteChatMessages(User user, Long processId) {
        try {
            getChatService().deleteChatMessages(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
