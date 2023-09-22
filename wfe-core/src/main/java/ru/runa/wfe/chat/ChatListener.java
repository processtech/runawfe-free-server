package ru.runa.wfe.chat;

import ru.runa.wfe.chat.dto.WfChatMessageBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;

public interface ChatListener {

    void onCreate(WfChatMessageBroadcast<MessageAddedBroadcast> wfChatMessageBroadcast);

    void onEdit(WfChatMessageBroadcast<MessageEditedBroadcast> wfChatMessageBroadcast);

    void onDelete(WfChatMessageBroadcast<MessageDeletedBroadcast> wfChatMessageBroadcast);
}
