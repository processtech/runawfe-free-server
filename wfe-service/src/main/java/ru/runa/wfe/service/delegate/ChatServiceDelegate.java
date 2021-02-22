package ru.runa.wfe.service.delegate;

import java.util.Collection;
import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
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
    public List<Long> getActiveChatIds(User user) {
        return getChatService().getActiveChatIds(user);
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
    public Collection<MessageAddedBroadcast> getMessages(User user, Long processId) {
        return getChatService().getMessages(user, processId);
    }

    @Override
    public List<Long> getNewMessagesCounts(User user, List<Long> processIds) {
        return getChatService().getNewMessagesCounts(user, processIds);
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
