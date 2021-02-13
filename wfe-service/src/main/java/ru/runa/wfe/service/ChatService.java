package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.user.User;

/**
 * Chat service.
 *
 * @author mrumyantsev
 * @since 02.02.2020
 */
public interface ChatService {

    public List<Long> getMentionedExecutorIds(User user, Long messageId);

    public List<Long> getActiveChatIds(User user);

    public MessageAddedBroadcast saveMessage(User user, Long processId, AddMessageRequest request);

    /**
     * Gets ChatMessage.
     *
     * @param id message Id
     * @return ChatMessage or <code>null</code>
     */
    public ChatMessage getMessage(User user, Long id);

    /**
     * Get List array of ChatMessage, where all "message Id" < firstId.
     *
     * @param processId chat Id
     * @param firstId   message Id, all returned message id < firstId
     * @param count     number of messages in the returned array
     * @return not <code>null</code> order by date desc
     */
    public List<MessageAddedBroadcast> getMessages(User user, Long processId, Long firstId, int count);

    /**
     * Get List array of ChatMessage, where all "message Id" >= lastId.
     *
     * @param processId chat Id
     * @param lastId    message Id, all returned message id >= lastId
     * @return not <code>null</code> order by date asc
     */
    public List<MessageAddedBroadcast> getNewChatMessages(User user, Long processId);

    /**
     * Get number of chat messages with id > lastMessageId.
     *
     * @param processId     chat Id
     * @param lastMessageId last message Id
     * @return number of chat messages with id > lastMessageId
     */
    public Long getNewChatMessagesCount(User user, Long processId);

    public List<Long> getNewMessagesCounts(User user, List<Long> processIds);

    public Long getLastMessage(User user, Long processId);

    public Long getLastReadMessage(User user, Long processId);

    /**
     * merge message in DB
     *
     * @param message message to merge
     * @return
     */
    public MessageEditedBroadcast updateMessage(User user, EditMessageRequest request);

    /**
     * Delete ChatMessage in DB.
     *
     * @param id message Id
     */
    public MessageDeletedBroadcast deleteMessage(User user, DeleteMessageRequest request);

    public void isReadMessage(User user, Long id);

//    /**
//     * Save ChatMessageFiles.
//     *
//     * @param file new file to save (associated message in ChatMessageFiles)
//     * @return not <code>null</code>
//     */
//    public ChatMessageFileDto saveChatMessageFile(User user, ChatMessageFileDto file);

    /**
     * Get ChatMessageFiles by id.
     *
     * @param fileId file Id
     * @return ChatMessageFiles or <code>null</code>
     */
    public ChatMessageFileDto getChatMessageFile(User user, Long fileId);

    /**
     * Get List array of all ChatMessageFiles in chat message.
     *
     * @param message chat message associated files
     * @return not <code>null</code>
     */
    public List<ChatMessageFileDto> getChatMessageFiles(User user, ChatMessage message);

//    public void deleteFile(User user, Long id);
}
