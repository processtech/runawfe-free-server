package ru.runa.wfe.chat.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.WfChatMessageBroadcast;

/**
 * @author Alekseev Mikhail
 * @since #2199
 */
@Component
public class BroadcastMessageTransactionListenerFactory {
    @Autowired
    private ChatSessionHandler sessionHandler;

    public BroadcastMessageTransactionListener createListener(WfChatMessageBroadcast<?> wfChatMessageBroadcast) {
        return new BroadcastMessageTransactionListener(sessionHandler, wfChatMessageBroadcast);
    }
}
