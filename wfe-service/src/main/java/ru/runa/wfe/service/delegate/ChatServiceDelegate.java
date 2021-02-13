package ru.runa.wfe.service.delegate;

import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
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
    public List<Long> getMentionedExecutorIds(User user, Long messageId) {
        return getChatService().getMentionedExecutorIds(user, messageId);
    }

    @Override
    public List<Long> getActiveChatIds(User user) {
        return getChatService().getActiveChatIds(user);
    }

    @Override
    public MessageAddedBroadcast saveMessage(User user, Long processId, AddMessageRequest request) {
        return getChatService().saveMessage(user, processId, request);
    }

    @Override
    public ChatMessage getMessage(User user, Long id) {
        return getChatService().getMessage(user, id);
    }

    @Override
    public List<MessageAddedBroadcast> getMessages(User user, Long processId, Long firstId, int count) {
        return getChatService().getMessages(user, processId, firstId, count);
    }

    @Override
    public List<MessageAddedBroadcast> getNewChatMessages(User user, Long processId) {
        return getChatService().getNewChatMessages(user, processId);
    }

    @Override
    public Long getNewChatMessagesCount(User user, Long processId) {
        return getChatService().getNewChatMessagesCount(user, processId);
    }

    @Override
    public List<Long> getNewMessagesCounts(User user, List<Long> processIds) {
        return getChatService().getNewMessagesCounts(user, processIds);
    }

    @Override
    public Long getLastMessage(User user, Long processId) {
        return getChatService().getLastMessage(user, processId);
    }

    @Override
    public Long getLastReadMessage(User user, Long processId) {
        return getChatService().getLastReadMessage(user, processId);
    }

    @Override
    public MessageEditedBroadcast updateMessage(User user, EditMessageRequest request) {
        return getChatService().updateMessage(user, request);
    }

    @Override
    public MessageDeletedBroadcast deleteMessage(User user, DeleteMessageRequest request) {
        return getChatService().deleteMessage(user, request);
    }

    @Override
    public void isReadMessage(User user, Long id) {
        getChatService().isReadMessage(user, id);
    }

//    @Override
//    public ChatMessageFileDto saveChatMessageFile(User user, ChatMessageFileDto file) {
//        return getChatService().saveChatMessageFile(user, file);
//    }

    @Override
    public ChatMessageFileDto getChatMessageFile(User user, Long fileId) {
        return getChatService().getChatMessageFile(user, fileId);
    }

    @Override
    public List<ChatMessageFileDto> getChatMessageFiles(User user, ChatMessage message) {
        return getChatService().getChatMessageFiles(user, message);
    }

//    @Override
//    public void deleteFile(User user, Long id) {
//        getChatService().deleteFile(user, id);
//    }


}
