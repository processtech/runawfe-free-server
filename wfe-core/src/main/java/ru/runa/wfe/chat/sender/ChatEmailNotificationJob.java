package ru.runa.wfe.chat.sender;

import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatEmailNotificationBuilder;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
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

    @Resource(name = "chatEmailNotificationJob")
    private ChatEmailNotificationJob self;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private ProcessDefinitionLoader definitionLoader;
    @Autowired
    private ChatMessageDao chatMessageDao;
    @Autowired
    private ChatFileDao chatFileDao;

    private static final int MESSAGE_LIMIT = 3;
    private byte[] configBytes;

    private final ChatEmailNotificationBuilder emailBuilder = new ChatEmailNotificationBuilder(MESSAGE_LIMIT);

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
        emailBuilder.setBaseUrl(baseUrl);
    }

    public void execute() {
        if (configBytes == null) {
            return;
        }
        Map<Actor, ChatEmailNotificationContext> contextsByActors;
        int page = 0;
        while (!(contextsByActors = self.getContextsByActorsWithPagination(page++, 10)).isEmpty()) {
            sendEmailsToActors(contextsByActors);
        }
    }

    private void sendEmailsToActors(Map<Actor, ChatEmailNotificationContext> contexts) {
        for (Map.Entry<Actor, ChatEmailNotificationContext> entry : contexts.entrySet()) {
            ChatEmailNotificationContext context = entry.getValue();
            List<String> emailsToSend = EmailUtils.getEmails(entry.getKey());
            String emails = EmailUtils.concatenateEmails(emailsToSend);
            EmailConfig config = EmailConfigParser.parse(configBytes);
            config.getHeaderProperties().put(EmailConfig.HEADER_TO, emails);
            config.getHeaderProperties().put("Subject", "Количество непрочитанных сообщений: " + context.getMessagesSize());
            config.setMessage(emailBuilder.build(context));
            try {
                EmailUtils.sendMessage(config);
            } catch (Exception e) {
                log.error("Email notification to: " + emails + " send error: " + e);
            }
        }
    }

    @Transactional(readOnly = true)
    public Map<Actor, ChatEmailNotificationContext> getContextsByActorsWithPagination(int pageIndex, int pageSize) {
        List<Actor> actors = executorDao.getAllActorsHaveEmailWithPagination(pageIndex, pageSize);
        Map<Actor, ChatEmailNotificationContext> result = new HashMap<>(actors.size());
        for (Actor actor : actors) {
            List<ChatMessage> messages = chatMessageDao.getNewMessagesByActor(actor);
            if (messages.isEmpty()) {
                continue;
            }
            ChatEmailNotificationContext context = new ChatEmailNotificationContext();
            context.setMessages(messages);
            context.setFiles(getFilesByMessages(messages));
            Set<Process> processes = context.getProcesses();
            context.setProcessesNames(getNamesByProcesses(processes));
            context.setPermissions(getPermissionByActorAndProcesses(actor, processes));
            result.put(actor, context);
        }
        return result;
    }


    private Map<ChatMessage, List<ChatMessageFile>> getFilesByMessages(List<ChatMessage> messages) {
        final Map<ChatMessage, List<ChatMessageFile>> result = new HashMap<>(messages.size());
        for (ChatMessage message : messages) {
            result.put(message, chatFileDao.getByMessage(message));
        }
        return result;
    }

    private Map<Process, String> getNamesByProcesses(Set<Process> processes) {
        Map<Process, String> result = new HashMap<>(processes.size());
        for (Process process : processes) {
            ProcessDefinition definition = definitionLoader.getDefinition(process);
            result.put(process, definition.getName());
        }
        return result;
    }

    private Map<Process, Boolean> getPermissionByActorAndProcesses(Actor actor, Set<Process> processes) {
        Map<Process, Boolean> result = new HashMap<>(processes.size());
        for (Process process : processes) {
            Boolean isAllowed = permissionDao.isAllowed(actor, Permission.READ, process.getSecuredObjectType(), process.getIdentifiableId());
            result.put(process, isAllowed);
        }
        return result;
    }
}
