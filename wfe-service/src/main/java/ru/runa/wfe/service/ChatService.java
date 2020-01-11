package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.user.Actor;

/**
 * Chat service.
 *
 * @author mrumyantsev
 * @since ___
 */
public interface ChatService {

    /**
     * 
     * 
     *
     * 
     *
     * 
     */
    public List<Long> getNewMessagesCounts(List<Long> chatsIds, List<Boolean> isMentions, Actor user);

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
    public boolean sendMessageToEmail(String title, String message, String Emaile);

    /**
     * get all users names for this chat
     *
     * @param processId
     *            chat id
     * @return List<Actor> or <code>null</code>
     */
    public List<Actor> getAllUsersNamesForChat(Long processId);

    /**
     * merge message in DB
     *
     * @param message
     *            message to merge
     */
    public void updateChatMessage(ChatMessage message);

    /**
     * validates the user (can edit message)
     *
     * @param user
     *            validated user (actor)
     * @return not <code>null</code>
     */
    public boolean canEditMessage(Actor user);

    /**
     * Get List array of all ChatMessageFiles in chat message.
     *
     * @param message
     *            chat message associated files
     * @return not <code>null</code>
     */
    public List<ChatMessageFile> getChatMessageFiles(ChatMessage message);

    /**
     * Get ChatMessageFiles by id.
     *
     * @param fileId
     *            file Id
     * @return ChatMessageFiles or <code>null</code>
     */
    public ChatMessageFile getChatMessageFile(Long fileId);

    /**
     * Save ChatMessageFiles.
     *
     * @param file
     *            new file to save (associated message in ChatMessageFiles)
     * @return not <code>null</code>
     */
    public ChatMessageFile saveChatMessageFile(ChatMessageFile file);

    /**
     * Get List array of all ChatMessage in chat.
     *
     * @param processId
     *            chat Id
     * @return not <code>null</code>
     */
    public List<ChatMessage> getChatMessages(Long processId);

    /**
     * Gets ChatMessage.
     *
     * @param messageId
     *            message Id
     * @return ChatMessage or <code>null</code>
     */
    public ChatMessage getChatMessage(Long messageId);

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
    public List<ChatMessage> getChatMessages(Long processId, Long firstId, int count);

    /**
     * Get List array of ChatMessage, where all "message Id" >= lastId.
     *
     * @param processId
     *            chat Id
     * @param lastId
     *            message Id, all returned message id >= lastId
     * @return not <code>null</code> order by date asc
     */
    public List<ChatMessage> getNewChatMessages(Long processId, Long lastId);

    /**
     * Get List array of last ChatMessage (first in the array of all messages).
     *
     * @param processId
     *            chat Id
     * @param count
     *            number of messages in the returned array
     * @return not <code>null</code>
     */
    public List<ChatMessage> getFirstChatMessages(Long processId, int count);

    /**
     * Save ChatMessage in DB.
     *
     * @param processId
     *            chat Id
     * @param message
     *            new message to save
     * @return new message id
     */
    public long saveChatMessage(Long processId, ChatMessage message);

    /**
     * Get number of chat messages.
     *
     * @param processId
     *            chat Id
     * @return number of chat messages
     */
    long getAllChatMessagesCount(Long processId);

    /**
     * Delete ChatMessage in DB.
     *
     * @param messId
     *            message Id
     */
    public void deleteChatMessage(Long messId);

    /**
     * Get ChatsUserInfo with information about the user in this chat or create new ChatsUserInfo with lastMessageId = max message Id in chat with
     * processId.
     *
     * @param processId
     *            chat Id
     * @param actor
     *            user Actor
     * @return ChatsUserInfo with information about the user in this chat, not <code>null</code>
     */
    public ChatsUserInfo getChatUserInfo(Actor actor, Long processId);

    /**
     * Get number of chat messages with id > lastMessageId.
     *
     * @param processId
     *            chat Id
     * @param lastMessageId
     *            last message Id
     * @return number of chat messages with id > lastMessageId
     */
    public long getNewChatMessagesCount(Long lastMessageId, Long processId);

    /**
     * update lastMessageId in ChatsUserInfo with userId, userName, processId.
     *
     * @param actor
     *            user Actor
     * @param processId
     *            chat Id
     * @param lastMessageId
     *            new lastMessageId in ChatsUserInfo
     */
    public void updateChatUserInfo(Actor actor, Long processId, Long lastMessageId);
}
