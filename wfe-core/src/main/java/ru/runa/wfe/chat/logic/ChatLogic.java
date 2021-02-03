package ru.runa.wfe.chat.logic;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatMessageDao;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class ChatLogic extends WfCommonLogic {
    private Properties properties = ClassLoaderUtil.getProperties("chat.email.properties", false);

    @Autowired
    private ChatMessageDao chatMessageDao;
    @Autowired
    private ChatFileLogic chatFileLogic;

    public ChatMessageDto saveMessageAndBindFiles(Actor actor, Long processId, ChatMessage message, Set<Executor> mentionedExecutors,
            Boolean isPrivate,
            ArrayList<ChatMessageFileDto> files) {
        message.setProcess(processDao.get(processId));
        Set<Executor> executors;
        if (!isPrivate) {
            executors = getAllUsers(processId, actor);
        } else {
            executors = new HashSet<>(mentionedExecutors);
        }
        List<ChatMessageFile> messageFiles = chatFileLogic.save(files, message);
        try {
            ChatMessageDto result = new ChatMessageDto(chatMessageDao.save(message, executors, mentionedExecutors));
            result.setFiles(messageFiles);
            return result;
        } catch (Exception exception) {
            chatFileLogic.deleteFiles(messageFiles);
            throw exception;
        }
    }

    public List<Long> getMentionedExecutorIds(Long messageId) {
        return chatMessageDao.getMentionedExecutorIds(messageId);
    }

    public void readMessage(Actor user, Long messageId) {
        chatMessageDao.readMessage(user, messageId);
    }

    public Long getLastReadMessage(Actor user, Long processId) {
        return chatMessageDao.getLastReadMessage(user, processId);
    }

    public Long getLastMessage(Actor user, Long processId) {
        return chatMessageDao.getLastMessage(user, processId);
    }

    public List<Long> getActiveChatIds(Actor user) {
        List<Long> ret = chatMessageDao.getActiveChatIds(user);
        if (ret == null) {
            ret = new ArrayList<Long>();
        }
        return ret;
    }

    public Set<Executor> getAllUsers(Long processId, Actor user) {
        Set<Executor> result = new HashSet<>();
        Process process = processDao.getNotNull(processId);
        List<Process> subProcesses = nodeProcessDao.getSubprocessesRecursive(process);
        {
            // select user from active tasks
            List<Task> tasks = new ArrayList<>();
            tasks.addAll(taskDao.findByProcess(process));
            for (Process subProcess : subProcesses) {
                tasks.addAll(taskDao.findByProcess(subProcess));
            }
            for (Task task : tasks) {
                Executor executor = task.getExecutor();
                if (executor instanceof Group) {
                    // TODO Do we want to store actor or group links?
                    result.addAll(executorDao.getGroupActors(((Group) executor)));
                } else if (executor instanceof Actor) {
                    result.add(executor);
                }
            }
        }
        {
            // select user from completed tasks
            List<ProcessLog> processLogs = new ArrayList<>();
            ProcessLogFilter filter = new ProcessLogFilter(processId);
            filter.setRootClassName(TaskEndLog.class.getName());
            processLogs.addAll(processLogDao.getAll(filter));
            for (Process subProcess : subProcesses) {
                filter.setProcessId(subProcess.getId());
                processLogs.addAll(processLogDao.getAll(filter));
            }
            for (ProcessLog processLog : processLogs) {
                String actorName = ((TaskEndLog) processLog).getActorName();
                try {
                    if (!Strings.isNullOrEmpty(actorName)) {
                        result.add(executorDao.getActor(actorName));
                    }
                } catch (ExecutorDoesNotExistException e) {
                    log.debug("Ignored deleted actor " + actorName + " for chat message");
                }
            }
        }
        {
            // пользователи имеющие права на чтение
            Set<Executor> executorWithPermission = permissionDao.getExecutorsWithPermission(process);
            for (Executor executor : executorWithPermission) {
                if (executor instanceof Group) {
                    // TODO Do we want to store actor or group links?
                    result.addAll(executorDao.getGroupActors(((Group) executor)));
                } else if (executor instanceof Actor) {
                    result.add(executor);
                }
            }
        }
        return result;
    }

    public List<Long> getNewMessagesCounts(List<Long> chatsIds, Actor user) {
        return chatMessageDao.getNewMessagesCounts(chatsIds, user);
    }

    public Long getNewMessagesCount(Actor user, Long processId) {
        return chatMessageDao.getNewMessagesCount(user, processId);
    }

    public ChatMessage getMessage(Actor actor, Long messageId) {
        return chatMessageDao.getMessage(messageId);
    }

    public ChatMessageDto getMessageDto(Long messageId) {
        return chatMessageDao.getMessageDto(messageId);
    }

    public List<ChatMessageDto> getMessages(Actor user, Long processId, Long firstId, int count) {
        return chatMessageDao.getMessages(user, processId, firstId, count);
    }

    public List<ChatMessageDto> getFirstMessages(Actor user, Long processId, int count) {
        return chatMessageDao.getFirstMessages(user, processId, count);
    }

    public List<ChatMessageDto> getNewMessages(Actor user, Long processId) {
        return chatMessageDao.getNewMessages(user, processId);
    }

    public ChatMessageDto saveMessage(Actor actor, Long processId, ChatMessage message, Set<Executor> mentionedExecutors, Boolean isPrivate) {
        message.setProcess(processDao.get(processId));
        if (!isPrivate) {
            Set<Executor> executors = getAllUsers(processId, message.getCreateActor());
            return new ChatMessageDto(chatMessageDao.save(message, executors, mentionedExecutors));
        } else {
            return new ChatMessageDto(chatMessageDao.save(message, mentionedExecutors, mentionedExecutors));
        }
    }

    public void deleteMessage(Actor actor, Long messageId) {
        ChatMessage message = getMessage(actor, messageId);
        List<ChatMessageFile> files = chatFileLogic.getFileByActorAndMessage(actor, message);
        chatMessageDao.deleteMessage(messageId);
        chatFileLogic.deleteFiles(files);
    }

    public void updateMessage(Actor actor, ChatMessage message) {
        chatMessageDao.updateMessage(message);
    }

    public void sendNotifications(ChatMessage chatMessage, Collection<Executor> executors) {
        if (properties.isEmpty()) {
            log.debug("chat.email.properties are not defined");
            return;
        }
        try {
            Set<String> emails = new HashSet<String>();
            for (Executor executor : executors) {
                if (executor instanceof Actor && StringUtils.isNotBlank(((Actor) executor).getEmail())) {
                    emails.add(((Actor) executor).getEmail());
                }
            }
            if (emails.isEmpty()) {
                log.debug("No emails found for " + chatMessage);
                return;
            }
            javax.mail.Session session = javax.mail.Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(properties.getProperty("login"), properties.getProperty("password"));
                }
            });
            Message mimeMessage = new MimeMessage(session);
            String titlePattern = (String) properties.get("title.pattern");
            String title = titlePattern//
                    .replace("$actorName", chatMessage.getCreateActor().getName())//
                    .replace("$processId", chatMessage.getProcess().getId().toString());
            String message = ((String) properties.get("message.pattern")).replace("$message", chatMessage.getText());
            mimeMessage.setFrom(new InternetAddress(properties.getProperty("login")));
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(Joiner.on(";").join(emails)));
            mimeMessage.setSubject(title);
            mimeMessage.setText(message);
            Transport.send(mimeMessage);
        } catch (Exception e) {
            log.warn("Unable to send chat email notification", e);
        }
    }

    public ChatMessageFileDto saveFile(ChatMessageFileDto file) {
        return chatFileLogic.save(file);
    }

    public List<ChatMessageFileDto> getFiles(Actor actor, ChatMessage message) {
        return chatFileLogic.getFilesByActorAndMessage(actor, message);
    }

    public ChatMessageFileDto getFile(Actor actor, Long fileId) {
        return chatFileLogic.getFileByActorAndId(actor, fileId);
    }

    public void deleteFile(User user, Long id) {
        chatFileLogic.deleteByUserAndId(user, id);
    }
}
