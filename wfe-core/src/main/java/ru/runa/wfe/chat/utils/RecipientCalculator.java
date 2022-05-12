package ru.runa.wfe.chat.utils;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;

@CommonsLog
@Component
@MonitoredWithSpring
public class RecipientCalculator {
    private static final String HTML_BR_TAG = "<br />";
    private static final String WHITESPACES_REGEX = "\\s+";
    private static final int LOGIN_BEGINNING_INDEX = 1;

    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private ExecutionLogic executionLogic;

    /**
     * @return set of actors who take part in given process them to see the message.
     * If message is private, return mentioned actors, defined by '@username' pattern
     */
    public Set<Actor> calculateRecipients(User user, boolean isPrivate, String messageText, Long processId) {
        final Set<Actor> recipients = executionLogic.getAllExecutorsByProcessId(user, processId, true);
        if (isPrivate) {
            recipients.retainAll(findMentionedActorsInMessageText(user, messageText));
        }

        return recipients;
    }

    private Set<Actor> findMentionedActorsInMessageText(User user, String messageText) {
        Set<Actor> recipients = new HashSet<>();
        for (String login : retrieveExecutorLogins(messageText)) {
            try {
                Executor executor = executorDao.getExecutor(login);
                if (executor instanceof Actor) {
                    recipients.add((Actor) executor);
                }
                if (executor instanceof Group) {
                    recipients.addAll(executorDao.getGroupActors((Group) executor));
                }
            } catch (ExecutorDoesNotExistException e) {
                log.debug("Ignored deleted executor " + login + " for chat message");
            }
        }
        log.info(recipients.size() + " mentioned actors were found");
        recipients.add(user.getActor());
        return recipients;
    }

    private Set<String> retrieveExecutorLogins(String messageText) {
        Set<String> logins = new HashSet<>();
        String[] words = messageText.replace(HTML_BR_TAG, " ").split(WHITESPACES_REGEX);
        for (String word : words) {
            if (word.startsWith("@")) {
                logins.add(word.substring(LOGIN_BEGINNING_INDEX));
            }
        }
        return logins;
    }
}
