package ru.runa.wfe.chat.sender;

import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Resource;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.NewMessagesForProcessTableBuilder;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatMessageDao;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.user.Actor;

/**
 * Created on 07.04.2021
 *
 * @author Sergey Inyakin
 * @since 2148
 */
@CommonsLog
public class ChatEmailNotificationJob {

    @Autowired
    private ChatMessageDao chatMessageDao;
    @Autowired
    private ChatFileDao chatFileDao;
    @Autowired
    private ChatEmailNotificationTransactionWrapper transactionWrapper;

    private static final int MESSAGE_LIMIT = 3;
    private byte[] configBytes;

    private String baseUrl;

    @Required
    public void setConfigLocation(String path) {
        try {
            InputStream configInputStream = ClassLoaderUtil.getAsStreamNotNull(path, getClass());
            configBytes = ByteStreams.toByteArray(configInputStream);
        } catch (Exception e) {
            log.error("Configuration error: " + e);
        }
    }

    @Required
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void execute() {
        if (configBytes == null) {
            return;
        }
        List<Actor> actors;
        int page = 0;
        while (!(actors = transactionWrapper.getAllActorsWithPagination(page++, 10)).isEmpty()) {
            sendEmailsToActors(actors);
        }
    }

    private void sendEmailsToActors(List<Actor> actors) {
        for (Actor actor : actors) {
            if (!actor.isActive()) {
                continue;
            }
            final ChatEmailNotificationConfigurator emailConfigurator = new ChatEmailNotificationConfigurator(configBytes);;
            emailConfigurator.setAddressesByActor(actor);
            if (emailConfigurator.isAddressesEmpty()) {
                continue;
            }
            final List<ChatMessage> messages = chatMessageDao.getNewMessagesByActor(actor);
            if (messages.isEmpty()) {
                continue;
            }
            emailConfigurator.setSubject(messages.size());
            final String message = createMessageForActor(actor, mappingByProcess(messages));
            emailConfigurator.sendMessage(message);
        }
    }

    private Map<Process, List<ChatMessage>> mappingByProcess(List<ChatMessage> messages) {
        final Map<Process, List<ChatMessage>> result = new HashMap<>();
        for (ChatMessage message : messages) {
            result.computeIfAbsent(message.getProcess(), new ComputeIfAbsentFunction()).add(message);
        }
        return result;
    }

    public String createMessageForActor(Actor actor, Map<Process, List<ChatMessage>> messagesByProcess) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Process, List<ChatMessage>> entry : messagesByProcess.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            Process process = entry.getKey();
            Map<ChatMessage, List<ChatMessageFile>> filesByMessages = getFilesByMessages(entry.getValue());
            ProcessDefinition definition = transactionWrapper.getProcessDefinition(process);
            NewMessagesForProcessTableBuilder builder = new NewMessagesForProcessTableBuilder(baseUrl, MESSAGE_LIMIT,
                    process, definition.getName(), filesByMessages);
            boolean isAllowed = transactionWrapper.isProcessReadAllowed(actor, process);
            result.append(builder.build(isAllowed));
        }
        return result.toString();
    }

    private Map<ChatMessage, List<ChatMessageFile>> getFilesByMessages(List<ChatMessage> messages) {
        final Map<ChatMessage, List<ChatMessageFile>> result = new LinkedHashMap<>(messages.size());
        int messageCount = 0;
        for (ChatMessage message : messages) {
            List<ChatMessageFile> files;
            if (messageCount++ < MESSAGE_LIMIT) {
                files = chatFileDao.getByMessage(message);
            } else {
                files = new ArrayList<>();
            }
            result.put(message, files);
        }
        return result;
    }

    private static class ComputeIfAbsentFunction implements Function<Process, List<ChatMessage>> {
        @Override
        public List<ChatMessage> apply(Process process) {
            return new ArrayList<>();
        }
    }
}
