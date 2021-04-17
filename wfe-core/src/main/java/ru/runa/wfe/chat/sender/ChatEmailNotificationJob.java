package ru.runa.wfe.chat.sender;

import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.NewMessagesForProcessTableBuilder;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatMessageDao;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

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
    private ExecutorDao executorDao;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private ProcessDefinitionLoader definitionLoader;

    private static final int MESSAGE_LIMIT = 3;
    private byte[] configBytes;
    private String baseUrl;
    private final Map<String, Set<Long>> sentMessagesForEmails = new HashMap<>();

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
        while (!(actors = executorDao.getAllActorsWithPagination(page++, 10)).isEmpty()) {
            for (Actor actor : actors) {
                List<String> emailsToSend = EmailUtils.getEmails(actor);
                if (emailsToSend.isEmpty()) {
                    continue;
                }
                String emails = EmailUtils.concatenateEmails(emailsToSend);
                List<ChatMessage> messages = chatMessageDao.getNewMessagesByActor(actor);
                if (messages.isEmpty() || isEmailAlreadySent(emails, messages)) {
                    continue;
                }
                EmailConfig config = EmailConfigParser.parse(configBytes);
                config.getHeaderProperties().put(EmailConfig.HEADER_TO, emails);
                config.clearMessage();
                config.getHeaderProperties().put("Subject", "Количество непрочитанных сообщений: " + messages.size());
                Map<Process, List<ChatMessage>> messagesByProcess = mappingByProcess(messages);
                try {
                    for (Map.Entry<Process, List<ChatMessage>> entry : messagesByProcess.entrySet()) {
                        if (entry.getValue().isEmpty()) {
                            continue;
                        }
                        Process process = entry.getKey();
                        ProcessDefinition processDefinition = definitionLoader.getDefinition(process);
                        boolean isAllowed = permissionDao.isAllowed(actor, Permission.READ, process.getSecuredObjectType(),
                                process.getIdentifiableId());
                        NewMessagesForProcessTableBuilder builder = new NewMessagesForProcessTableBuilder(baseUrl, entry.getKey(),
                                processDefinition.getName(), getFilesByMessages(entry.getValue()), isAllowed, MESSAGE_LIMIT);
                        config.setMessage(config.getMessage() + builder.build());
                    }
                    if (config.getMessage().isEmpty()) {
                        continue;
                    }
                    EmailUtils.sendMessage(config);
                    setSentMessagesForEmails(messages, emails);
                } catch (Exception e) {
                    log.error("Email notification to: " + emails + " send error: " + e);
                }
            }
        }
    }

    private boolean isEmailAlreadySent(String emails, List<ChatMessage> messages) {
        if (!sentMessagesForEmails.containsKey(emails) || sentMessagesForEmails.get(emails).size() != messages.size()) {
            return false;
        }
        Set<Long> messagesId = new HashSet<>(messages.size());
        for (ChatMessage message : messages) {
            messagesId.add(message.getId());
        }
        return Objects.equals(messagesId, sentMessagesForEmails.get(emails));
    }

    private void setSentMessagesForEmails(List<ChatMessage> messages, String emails) {
        Set<Long> sentMessages = new HashSet<>(messages.size());
        for (ChatMessage message : messages) {
            sentMessages.add(message.getId());
        }
        sentMessagesForEmails.put(emails, sentMessages);
    }

    private Map<Process, List<ChatMessage>> mappingByProcess(List<ChatMessage> messages) {
        Map<Process, List<ChatMessage>> result = new HashMap<>();
        for (ChatMessage message : messages) {
            (result.computeIfAbsent(message.getProcess(), new Function<Process, List<ChatMessage>>() {
                @Override
                public List<ChatMessage> apply(Process process) {
                    return new ArrayList<>();
                }
            })).add(message);
        }
        return result;
    }

    private Map<ChatMessage, List<ChatMessageFile>> getFilesByMessages(List<ChatMessage> messages) {
        Map<ChatMessage, List<ChatMessageFile>> result = new LinkedHashMap<>(messages.size());
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
}
