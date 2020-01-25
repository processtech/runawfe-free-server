package ru.runa.wfe.chat.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageDto;
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

    private Properties properties = ClassLoaderUtil.getProperties("chat.properties", true);

    public List<Long> getMentionedExecutorIds(Long messageId) {
        return chatDao.getMentionedExecutorIds(messageId);
    }

    public void deleteFile(User user, Long id) {
        chatDao.deleteFile(user, id);
    }

    public Long saveMessageAndBindFiles(User user, ChatMessage message, Set<Executor> mentionedExecutors, Boolean isPrivate,
            ArrayList<Long> fileIds) {
        Set<Executor> executors;
        if (!isPrivate) {
            executors = getAllUsers(message.getProcessId(), message.getCreateActor());
        } else {
            executors = new HashSet<Executor>(mentionedExecutors);
        }
        return chatDao.saveMessageAndBindFiles(user, message, fileIds, executors, mentionedExecutors);
    }

    public void readMessage(Actor user, Long messageId) {
        chatDao.readMessage(user, messageId);
    }

    public Long getLastReadMessage(Actor user, Long processId) {
        return chatDao.getLastReadMessage(user, processId);
    }

    public List<Long> getActiveChatIds(Actor user) {
        List<Long> ret = chatDao.getActiveChatIds(user);
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
                    // TODO подумать - хотим мы хранить в получателях ссылки на группы или на пользователей
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
                    result.add(executorDao.getActor(actorName));
                } catch (ExecutorDoesNotExistException e) {
                    log.debug("Ignored deleted actor " + actorName + " for chat message");
                }
            }
        }
        return result;
    }

    public List<Long> getNewMessagesCounts(List<Long> chatsIds, List<Boolean> isMentions, Actor user) {
        return chatDao.getNewMessagesCounts(chatsIds, isMentions, user);
    }

    public Long getNewMessagesCount(Actor user, Long processId) {
        return chatDao.getNewMessagesCount(user, processId);
    }

    public ChatMessage getMessage(Long messageId) {
        return chatDao.getMessage(messageId);
    }

    public ChatMessageDto getMessageDto(Long messageId) {
        return chatDao.getMessageDto(messageId);
    }

    public List<ChatMessageDto> getMessages(Actor user, Long processId, Long firstId, int count) {
        return chatDao.getMessages(user, processId, firstId, count);
    }

    public List<ChatMessageDto> getFirstMessages(Actor user, Long processId, int count) {
        return chatDao.getFirstMessages(user, processId, count);
    }

    public List<ChatMessageDto> getNewMessages(Actor user, Long processId) {
        return chatDao.getNewMessages(user, processId);
    }

    public Long saveMessage(Long processId, ChatMessage message, Set<Executor> mentionedExecutors, Boolean isPrivate) {
        if (!isPrivate) {
            Set<Executor> executors = getAllUsers(processId, message.getCreateActor());
            return chatDao.save(message, executors, mentionedExecutors);
        } else {
            return chatDao.save(message, mentionedExecutors, mentionedExecutors);
        }
    }

    public void deleteMessage(Long messId) {
        chatDao.deleteMessageFiles(messId);
        chatDao.deleteMessage(messId);
    }

    public List<ChatMessageFile> getMessageFiles(Actor actor, ChatMessage message) {
        return chatDao.getMessageFiles(actor, message);
    }

    public ChatMessageFile saveFile(ChatMessageFile file) {
        return chatDao.saveFile(file);
    }

    public ChatMessageFile getFile(Actor actor, Long fileId) {
        return chatDao.getFile(actor, fileId);
    }

    public void updateMessage(ChatMessage message) {
        chatDao.updateMessage(message);
    }

    public Boolean canEditMessage(Actor user) {
        return true;
    }

    public Boolean sendMessageToEmail(String title, String message, String Emaile) {
        // Создаем соединение для отправки почтового сообщения
        javax.mail.Session session = javax.mail.Session.getDefaultInstance(properties,
        // Аутентификатор - объект, который передает логин и пароль
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(properties.getProperty("chat.email.login"), properties.getProperty("chat.email.password"));
                    }
                });
        // Создаем новое почтовое сообщение
        Message mimeMessage = new MimeMessage(session);
        try {
            // От кого
            mimeMessage.setFrom(new InternetAddress(properties.getProperty("chat.email.login")));
            // Кому
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(Emaile));
            // Тема письма
            mimeMessage.setSubject(title);
            // Текст письма
            mimeMessage.setText(message);
            // отправка
            Transport.send(mimeMessage);
        } catch (AddressException e) {
            return false;
        } catch (MessagingException e) {
            return false;
        }
        return true;
    }

}
