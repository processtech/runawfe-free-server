package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.User;

/**
 * Chat service.
 *
 * @author mrumyantsev
 * @since 02.02.2020
 */
public interface ChatService {

    /**
     * Saves a new message and sends the <code>MessageAddedBroadcast<code> to all active chats
     *
     * @param user
     *              authorized user
     * @param request
     *              request to add a new message
     */
    public void saveMessage(User user, AddMessageRequest request);

    /**
     * Gets ChatMessage.
     *
     * @param id
     *              message Id
     * @return ChatMessage or <code>null</code>
     */
    public ChatMessage getMessage(User user, Long id);

    /**
     * Get List array of ChatMessage, where all "message Id" < firstId.
     *
     * @param processId
     *              chat Id
     * @return not <code>null</code> order by date desc
     */
    public List<MessageAddedBroadcast> getMessages(User user, Long processId);

    /**
     * Get new messages count for a concrete user.
     *
     * @param user
     *              authorized user, the count of new messages for which will be returned
     *
     * @return new messages count
     */
    public Long getNewMessagesCount(User user);

    /**
     * Get chats count
     *
     * @param user
     *              authorized user
     * @param batchPresentation
     *              batch presentation
     * @return chats count
     */
    public int getChatRoomsCount(User user, BatchPresentation batchPresentation);

    /**
     * Gets a list of chats
     * @param user
     *              authorized user
     * @param batchPresentation
     *              batch presentation
     * @return not <code>null</code>
     */
    public List<WfChatRoom> getChatRooms(User user, BatchPresentation batchPresentation);

    /**
     * Updates the message and sends the <code>MessageEditedBroadcast<code> to all active chats
     *
     * @param request
     *              request to edit message
     */
    public void updateMessage(User user, EditMessageRequest request);

    /**
     * Deletes the message and sends the <code>MessageDeletedBroadcast<code> to all active chats
     *
     * @param request
     *              request to delete message
     */
    public void deleteMessage(User user, DeleteMessageRequest request);

    /**
     * Get <code>ChatMessageFilesDto</code> by id.
     *
     * @param fileId
     *              file Id
     * @return ChatMessageFiles or <code>null</code>
     */
    public ChatMessageFileDto getChatMessageFile(User user, Long fileId);

    /**
     * Delete ChatMessages in DB.
     *
     * @param processId
     *            process Id
     */
    public void deleteChatMessages(User user, Long processId);
}
