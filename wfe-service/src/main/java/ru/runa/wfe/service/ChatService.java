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
     * @param chatId
     *            chat id
     * @return List<Actor> or <code>null</code>
     */
    public List<Actor> getAllUsersNamesForChat(int chatId);

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
    public ChatMessageFile getChatMessageFile(long fileId);

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
     * @param chatId
     *            chat Id
     * @return not <code>null</code>
     */
    public List<ChatMessage> getChatMessages(int chatId);

    /**
     * Gets ChatMessage.
     *
     * @param messageId
     *            message Id
     * @return ChatMessage or <code>null</code>
     */
    public ChatMessage getChatMessage(long messageId);

    /**
     * Get List array of ChatMessage, where all "message Id" < firstId.
     *
     * @param chatId
     *            chat Id
     * @param firstId
     *            message Id, all returned message id < firstId
     * @param count
     *            number of messages in the returned array
     * @return not <code>null</code> order by date desc
     */
    public List<ChatMessage> getChatMessages(int chatId, Long firstId, int count);

    /**
     * Get List array of ChatMessage, where all "message Id" >= lastId.
     *
     * @param chatId
     *            chat Id
     * @param lastId
     *            message Id, all returned message id >= lastId
     * @return not <code>null</code> order by date asc
     */
    public List<ChatMessage> getNewChatMessages(int chatId, Long lastId);

    /**
     * Get List array of last ChatMessage (first in the array of all messages).
     *
     * @param chatId
     *            chat Id
     * @param count
     *            number of messages in the returned array
     * @return not <code>null</code>
     */
    public List<ChatMessage> getFirstChatMessages(int chatId, int count);

    /**
     * Save ChatMessage in DB.
     *
     * @param chatId
     *            chat Id
     * @param message
     *            new message to save
     * @return new message id
     */
    public long saveChatMessage(int chatId, ChatMessage message);

    /**
     * Get number of chat messages.
     *
     * @param chatId
     *            chat Id
     * @return number of chat messages
     */
    long getAllChatMessagesCount(int chatId);

    /**
     * Delete ChatMessage in DB.
     *
     * @param messId
     *            message Id
     */
    public void deleteChatMessage(long messId);

    /**
     * Get List array of all chat Id Connected with "chatId" (and "chatId"). To combine chats.
     *
     * @param chatId
     *            chat Id
     * @return List array of Connected chat Id and chatId (Integer), not <code>null</code>
     */
    public List<Integer> getAllConnectedChatId(int chatId);

    /**
     * Get ChatsUserInfo with information about the user in this chat or create new ChatsUserInfo with lastMessageId = max message Id in chat with
     * chatId.
     *
     * @param chatId
     *            chat Id
     * @param actor
     *            user Actor
     * @return ChatsUserInfo with information about the user in this chat, not <code>null</code>
     */
    public ChatsUserInfo getChatUserInfo(Actor actor, int chatId);

    /**
     * Get number of chat messages with id > lastMessageId.
     *
     * @param chatId
     *            chat Id
     * @param lastMessageId
     *            last message Id
     * @return number of chat messages with id > lastMessageId
     */
    public long getNewChatMessagesCount(long lastMessageId, int chatId);

    /**
     * update lastMessageId in ChatsUserInfo with userId, userName, chatId.
     *
     * @param actor
     *            user Actor
     * @param chatId
     *            chat Id
     * @param lastMessageId
     *            new lastMessageId in ChatsUserInfo
     */
    public void updateChatUserInfo(Actor actor, int chatId, long lastMessageId);
}
