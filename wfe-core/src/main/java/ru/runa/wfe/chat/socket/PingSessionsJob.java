package ru.runa.wfe.chat.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Alekseev Mikhail
 * @since #2178
 */
public class PingSessionsJob {
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Scheduled(fixedDelayString = "${timertask.period.millis.ping.chat.sessions}")
    public void execute() {
        sessionHandler.ping();
    }
}
