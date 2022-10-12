package ru.runa.wfe.chat.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.handler.ChatSessionHandler;

/**
 * @author Alekseev Mikhail
 * @since #2178
 */
@Component
public class PingSessionsJob {
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Scheduled(fixedDelayString = "${timertask.period.millis.ping.chat.sessions}")
    public void execute() {
        sessionHandler.ping();
    }
}
