package ru.runa.wfe.chat.sender;

import com.google.common.io.ByteStreams;
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
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

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
    private ChatMessageDao chatMessageDao;
    @Autowired
    private ChatFileDao chatFileDao;

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
        Map<Actor, ChatEmailNotificationBuilder> buildersByActors;
        int page = 0;
        do {
            buildersByActors = self.getEmailBuildersByActorsWithPagination(page++, 10);
            sendEmailsToActors(buildersByActors);
        } while (!buildersByActors.isEmpty());
    }

    @Transactional(readOnly = true)
    public Map<Actor, ChatEmailNotificationBuilder> getEmailBuildersByActorsWithPagination(int pageIndex, int pageSize) {
        List<Actor> actors = executorDao.getAllActorsHaveEmailWithPagination(pageIndex, pageSize);
        Map<Actor, ChatEmailNotificationBuilder> result = new HashMap<>(actors.size());
        for (Actor actor : actors) {
            List<ChatMessage> messages = chatMessageDao.getNewMessagesByActor(actor);
            Map<Process, List<ChatMessage>> messagesByProcesses = getMessagesByProcesses(messages);
            ChatEmailNotificationBuilder emailBuilder = new ChatEmailNotificationBuilder()
                    .baseUrl(baseUrl)
                    .newMessagesCount(messages.size())
                    .actor(actor)
                    .messages(messagesByProcesses)
                    .files(getFilesByMessages(messages))
                    .processesNames(getDefinitionNamesByProcesses(messagesByProcesses.keySet()))
                    .permissions(getPermissionByActorAndProcesses(actor, messagesByProcesses.keySet()));
            result.put(actor, emailBuilder);
        }
        return result;
    }

    private void sendEmailsToActors(Map<Actor, ChatEmailNotificationBuilder> buildersByActor) {
        for (Map.Entry<Actor, ChatEmailNotificationBuilder> entry : buildersByActor.entrySet()) {
            ChatEmailNotificationBuilder emailBuilder = entry.getValue();
            if (emailBuilder.isNoNewMessages()) {
                continue;
            }
            try {
                EmailConfig config = emailBuilder.build(configBytes);
                EmailUtils.sendMessage(config);
            } catch (Exception e) {
                Actor actor = entry.getKey();
                log.error("Email notification to: " + actor.getEmail() + " send error: " + e);
            }
        }
    }

    private Map<Process, List<ChatMessage>> getMessagesByProcesses(List<ChatMessage> messages) {
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

    private Map<Process, String> getDefinitionNamesByProcesses(Set<Process> processes) {
        Map<Process, String> result = new HashMap<>(processes.size());
        for (Process process : processes) {
            result.put(process, process.getDeployment().getName());
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
