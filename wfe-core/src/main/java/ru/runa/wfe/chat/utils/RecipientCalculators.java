package ru.runa.wfe.chat.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RecipientCalculators {

    private final ExecutorLogic executorLogic;
    private final ExecutionLogic executionLogic;

    public Collection<Long> calculateRecipientIds(Set<Actor> recipients) {
        Collection<Long> recipientIds = new HashSet<>();
        for (Actor actor : recipients) {
            recipientIds.add(actor.getId());
        }
        return recipientIds;
    }

    public Set<Actor> calculateRecipients(User user, boolean isPrivate, String messageText, Long processId) {
        return isPrivate
                ? findMentionedActorsInMessageText(user, messageText)
                : getAllActorsByProcessId(processId);
    }

    /**
     * @return Mentioned actors, defined by '@username' pattern
     */
    private Set<Actor> findMentionedActorsInMessageText(User user, String messageText) {
        Set<Actor> recipients = new HashSet<>();
        int dogIndex = -1;
        while (true) {
            dogIndex = messageText.indexOf('@', dogIndex + 1);
            if (dogIndex != -1) {
                int spaceIndex = messageText.indexOf(' ', dogIndex);
                String login = (spaceIndex != -1)
                        ? messageText.substring(dogIndex + 1, spaceIndex)
                        : messageText.substring(dogIndex + 1);
                Executor executor = executorLogic.getExecutor(user, login);
                if (executor instanceof Actor) {
                    recipients.add((Actor) executor);
                }
            } else {
                break;
            }
        }
        return recipients;
    }

    private Set<Actor> getAllActorsByProcessId(Long processId) {
        Set<Actor> recipients = new HashSet<>();
        for (Executor executor : executionLogic.getAllExecutorsByProcessId(processId)) {
            if (executor instanceof Actor) {
                recipients.add((Actor) executor);
            }
        }
        return recipients;
    }
}
