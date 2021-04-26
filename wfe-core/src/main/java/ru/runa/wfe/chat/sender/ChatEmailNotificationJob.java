package ru.runa.wfe.chat.sender;

import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    private byte[] configBytes;
    private String baseUrl;
    private boolean isNextPage;

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
        Map<Actor, ChatEmailNotificationBuilder> buildersByActors;
        int page = 0;
        do {
            buildersByActors = self.getEmailBuildersByActors(page++, 10);
            sendEmailsToActors(buildersByActors);
        } while (isNextPage);
    }

    @Transactional(readOnly = true)
    public Map<Actor, ChatEmailNotificationBuilder> getEmailBuildersByActors(int pageIndex, int pageSize) {
        List<Actor> actors = executorDao.getAllActorsWithPagination(pageIndex, pageSize);
        isNextPage = !actors.isEmpty();
        Map<Actor, ChatEmailNotificationBuilder> result = new HashMap<>(actors.size());
        for (Actor actor : actors) {
            if (!actor.isActive() || actor.getEmail() == null || actor.getEmail().isEmpty()) {
                continue;
            }
            List<ChatMessage> messages = chatMessageDao.getNewMessagesByActor(actor);
            if (messages.isEmpty()) {
                continue;
            }
            Map<Process, List<ChatMessage>> messagesByProcesses = mappedProcessesByMessages(messages);
            ChatEmailNotificationBuilder emailBuilder = new ChatEmailNotificationBuilder()
                    .setBaseUrl(baseUrl)
                    .setNewMessagesCount(messages.size())
                    .setActor(actor)
                    .setMessages(messagesByProcesses)
                    .setFiles(getFilesByMessages(messages))
                    .setProcessesNames(getNamesByProcesses(messagesByProcesses.keySet()))
                    .setPermissions(getPermissionByActorAndProcesses(actor, messagesByProcesses.keySet()));
            result.put(actor, emailBuilder);
        }
        return result;
    }

    private void sendEmailsToActors(Map<Actor, ChatEmailNotificationBuilder> contexts) {
        for (Map.Entry<Actor, ChatEmailNotificationBuilder> entry : contexts.entrySet()) {
            ChatEmailNotificationBuilder emailBuilder = entry.getValue();
            try {
                EmailConfig config = emailBuilder.build(configBytes);
                EmailUtils.sendMessage(config);
            } catch (Exception e) {
                Actor actor = entry.getKey();
                log.error("Email notification to: " + actor.getEmail() + " send error: " + e);
            }
        }
    }

    private Map<Process, List<ChatMessage>> mappedProcessesByMessages(List<ChatMessage> messages) {
        Map<Process, List<ChatMessage>> result = new HashMap<>();
        for (ChatMessage message : messages) {
            result.computeIfAbsent(message.getProcess(), new ComputeIfAbsentFunction()).add(message);
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

    private static class ComputeIfAbsentFunction implements Function<Process, List<ChatMessage>> {
        @Override
        public List<ChatMessage> apply(Process process) {
            return new ArrayList<>();
        }
    }
}
