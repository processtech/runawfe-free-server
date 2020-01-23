package ru.runa.wfe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Chat service.
 *
 * @author mrumyantsev
 * @since ___
 */
public interface ChatService {

    public void deleteFile(User user, Long id);

    public Long saveMessageAndBindFiles(User user, ChatMessage message, ArrayList<Long> fileIds);

    public void readMessage(User user, Long messageId);

    public Long getLastReadMessage(User user, Long processId);

    public List<Long> getActiveChatIds(User user);

    public Set<Executor> getAllUsers(User user, Long processId);

    /**
     * 
     * 
     *
     * 
     *
     * 
     */
    public List<Long> getNewMessagesCounts(User user, List<Long> processIds, List<Boolean> isMentions);

    /**
     * send by email
     * 
     * @param title
     *            message subject
     * @param message
     *            message
     * @param Emaile
     *            email address (full)
     *
     * @return sends an email (chat emaile - chat.properties)
     */
    public Boolean sendMessageToEmail(User user, String title, String message, String Emaile);

    /**
     * merge message in DB
     *
     * @param message
     *            message to merge
     */
    public void updateChatMessage(User user, ChatMessage message);

    /**
     * validates the user (can edit message)
     *
     * @param user
     *            validated user (actor)
     * @return not <code>null</code>
     */
    public Boolean canEditMessage(User user);

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
     * Get List array of all ChatMessage in chat.
     *
     * @param processId
     *            chat Id
     * @return not <code>null</code>
     */
    public List<ChatMessage> getChatMessages(User user, Long processId);

    /**
     * Gets ChatMessage.
     *
     * @param messageId
     *            message Id
     * @return ChatMessage or <code>null</code>
     */
    public ChatMessage getChatMessage(User user, Long messageId);

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
    public List<ChatMessage> getChatMessages(User user, Long processId, Long firstId, int count);

    /**
     * Get List array of ChatMessage, where all "message Id" >= lastId.
     *
     * @param processId
     *            chat Id
     * @param lastId
     *            message Id, all returned message id >= lastId
     * @return not <code>null</code> order by date asc
     */
    public List<ChatMessage> getNewChatMessages(User user, Long processId);

    /**
     * Get List array of last ChatMessage (first in the array of all messages).
     *
     * @param processId
     *            chat Id
     * @param count
     *            number of messages in the returned array
     * @return not <code>null</code>
     */
    public List<ChatMessage> getFirstChatMessages(User user, Long processId, int count);

    /**
     * Save ChatMessage in DB.
     *
     * @param processId
     *            chat Id
     * @param message
     *            new message to save
     * @return new message id
     */
    public Long saveChatMessage(User user, Long processId, ChatMessage message);

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
