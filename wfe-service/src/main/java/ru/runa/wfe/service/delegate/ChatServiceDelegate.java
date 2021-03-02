package ru.runa.wfe.service.delegate;

import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
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
    public List<WfChatRoom> getChatRooms(User user) {
        return getChatService().getChatRooms(user);
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

}
