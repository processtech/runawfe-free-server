package ru.runa.wfe.chat.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MentionedExecutorsExtractor {

    private final ExecutorLogic executorLogic;

    /**
     * @return Mentioned executors, defined by '@user' pattern
     */
    public Set<Executor> extractMentionedExecutors(String privateNames, ChatMessage newMessage, User user) {
        Set<Executor> mentionedExecutors = new HashSet<>();
        String[] loginsPrivateTable = privateNames != null ? privateNames.split(";") : new String[0];
        String messageText = newMessage.getText();
        while (true) {
            int dogIndex = messageText.indexOf('@');
            if (dogIndex != -1) {
                int spaceIndex = messageText.indexOf(' ', dogIndex);
                String login = (spaceIndex != -1) ?
                        messageText.substring(dogIndex + 1, spaceIndex) : messageText.substring(dogIndex + 1);
                Executor executor = executorLogic.getExecutor(user, login);
                if (executor != null) {
                    mentionedExecutors.add(executor);
                }
            } else {
                if ((loginsPrivateTable.length > 0) && (loginsPrivateTable[0].trim().length() != 0)) {
                    for (String loginPrivate : loginsPrivateTable) {
                        Executor executor = executorLogic.getExecutor(user, loginPrivate);
                        if (executor != null) {
                            mentionedExecutors.add(executor);
                        }
                    }
                }
                break;
            }
        }
        return mentionedExecutors;
    }

    public Collection<Long> extractRecipientIds(Set<Executor> mentionedExecutors, boolean isPrivate) {
        Collection<Long> recipientIds = new HashSet<>();
        if (isPrivate) {
            for (Executor mentionedExecutor : mentionedExecutors) {
                if (mentionedExecutor instanceof Actor) {
                    recipientIds.add(mentionedExecutor.getId());
                }
            }
        } else {
            // TODO #1934 Generate recipient Ids if the message is not private!
        }
        return recipientIds;
    }
}
