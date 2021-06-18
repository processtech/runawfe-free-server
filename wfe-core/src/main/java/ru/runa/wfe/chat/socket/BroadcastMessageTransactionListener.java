package ru.runa.wfe.chat.socket;

import javax.transaction.UserTransaction;
import lombok.RequiredArgsConstructor;
import ru.runa.wfe.chat.dto.WfChatMessageBroadcast;
import ru.runa.wfe.commons.TransactionListener;

/**
 * @author Alekseev Mikhail
 * @since #2199
 */
@RequiredArgsConstructor
public class BroadcastMessageTransactionListener implements TransactionListener {
    private final ChatSessionHandler sessionHandler;
    private final WfChatMessageBroadcast<?> wfChatMessageBroadcast;

    @Override
    public void onTransactionComplete(UserTransaction transaction) {
        sessionHandler.sendMessage(wfChatMessageBroadcast);
    }
}
