package ru.runa.wfe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.broadcast.AddedMessageBroadcast;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Chat service.
 *
 * @author mrumyantsev
 * @since 02.02.2020
 */
public interface ChatService {

    public List<Long> getMentionedExecutorIds(User user, Long messageId);

    public void deleteFile(User user, Long id);

    public AddedMessageBroadcast saveMessageAndBindFiles(User user, Long processId, ChatMessage message, Set<Executor> mentionedExecutors, Boolean isPrivate,
                                                    ArrayList<ChatMessageFile> files);

    public void readMessage(User user, Long messageId);

    public Long getLastReadMessage(User user, Long processId);

    public Long getLastMessage(User user, Long processId);

    public List<Long> getActiveChatIds(User user);

    public Set<Executor> getAllUsers(User user, Long processId);

    public List<Long> getNewMessagesCounts(User user, List<Long> processIds);

    /**
     * merge message in DB
     *
     * @param message
     *            message to merge
     */
    public void updateChatMessage(User user, ChatMessage message);

    /**
     * Get List array of all ChatMessageFiles in chat message.
     *
     * @param message
     *            chat message associated files
     * @return not <code>null</code>
     */
    public List<ChatMessageFile> getChatMessageFiles(User user, ChatMessage message);

    /**
     * Get ChatMessageFiles by id.
     *
     * @param fileId
     *            file Id
     * @return ChatMessageFiles or <code>null</code>
     */
    public ChatMessageFile getChatMessageFile(User user, Long fileId);

    /**
     * Save ChatMessageFiles.
     *
     * @param file
     *            new file to save (associated message in ChatMessageFiles)
     * @return not <code>null</code>
     */
    public ChatMessageFile saveChatMessageFile(User user, ChatMessageFile file);

    /**
     * Gets ChatMessage.
     *
     * @param messageId
     *            message Id
     * @return ChatMessage or <code>null</code>
     */
    public ChatMessage getChatMessage(User user, Long messageId);

    public AddedMessageBroadcast getChatMessageDto(User user, Long messageId);

    /**
     * Get List array of ChatMessage, where all "message Id" < firstId.
     *
     * @param processId
     *            chat Id
     * @param firstId
     *            message Id, all returned message id < firstId
     * @param count
     *            number of messages in the returned array
     * @return not <code>null</code> order by date desc
     */
    public List<AddedMessageBroadcast> getChatMessages(User user, Long processId, Long firstId, int count);

    /**
     * Get List array of ChatMessage, where all "message Id" >= lastId.
     *
     * @param processId
     *            chat Id
     * @param lastId
     *            message Id, all returned message id >= lastId
     * @return not <code>null</code> order by date asc
     */
    public List<AddedMessageBroadcast> getNewChatMessages(User user, Long processId);

    /**
     * Get List array of last ChatMessage (first in the array of all messages).
     *
     * @param processId
     *            chat Id
     * @param count
     *            number of messages in the returned array
     * @return not <code>null</code>
     */
    public List<AddedMessageBroadcast> getFirstChatMessages(User user, Long processId, int count);

    /**
     * Save ChatMessage in DB.
     *
     * @param processId
     *            chat Id
     * @param message
     *            new message to save
     * @return new message id
     */
    public Long saveChatMessage(User user, Long processId, ChatMessage message, Set<Executor> mentionedExecutors, Boolean isPrivate);

    /**
     * Delete ChatMessage in DB.
     *
     * @param messId
     *            message Id
     */
    public void deleteChatMessage(User user, Long messId);

    /**
     * Get number of chat messages with id > lastMessageId.
     *
     * @param processId
     *            chat Id
     * @param lastMessageId
     *            last message Id
     * @return number of chat messages with id > lastMessageId
     */
    public Long getNewChatMessagesCount(User user, Long processId);
}
