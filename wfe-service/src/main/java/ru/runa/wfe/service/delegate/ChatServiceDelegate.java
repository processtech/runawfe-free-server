package ru.runa.wfe.service.delegate;

import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
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
    public void saveMessage(User user, AddMessageRequest request) {
        getChatService().saveMessage(user, request);
    }

    @Override
    public ChatMessage getMessage(User user, Long id) {
        return getChatService().getMessage(user, id);
    }

    @Override
    public List<MessageAddedBroadcast> getMessages(User user, Long processId) {
        return getChatService().getMessages(user, processId);
    }

    @Override
    public Long getNewMessagesCount(User user) {
        return getChatService().getNewMessagesCount(user);
    }

    @Override
    public int getChatRoomsCount(User user, BatchPresentation batchPresentation) {
        return getChatService().getChatRoomsCount(user, batchPresentation);
    }

    @Override
    public List<WfChatRoom> getChatRooms(User user, BatchPresentation batchPresentation) {
        return getChatService().getChatRooms(user, batchPresentation);
    }

    @Override
    public void updateMessage(User user, EditMessageRequest request) {
        getChatService().updateMessage(user, request);
    }

    @Override
    public void deleteMessage(User user, DeleteMessageRequest request) {
        getChatService().deleteMessage(user, request);
    }

    @Override
    public ChatMessageFileDto getChatMessageFile(User user, Long fileId) {
        return getChatService().getChatMessageFile(user, fileId);
    }

    @Override
    public void deleteChatMessages(User user, Long processId) {
        getChatService().deleteChatMessages(user, processId);
    }

}
