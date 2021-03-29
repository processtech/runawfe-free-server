package ru.runa.wfe.chat.socket;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Alekseev Mikhail
 * @since #2178
 */
public class PingSessionsJob {
    @Autowired
    private ChatSessionHandler sessionHandler;

    public void execute() {
        sessionHandler.ping();
    }
}
