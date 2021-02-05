package ru.runa.wfe.chat.logic;

import com.google.common.base.Joiner;
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
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatDao;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class ChatLogic extends WfCommonLogic {
    private Properties properties = ClassLoaderUtil.getProperties("chat.email.properties", false);

    @Autowired
    private ChatDao chatDao;

    public List<Long> getMentionedExecutorIds(Long messageId) {
        return chatDao.getMentionedExecutorIds(messageId);
    }

    public void deleteFile(User user, Long id) {
        chatDao.deleteFile(user, id);
    }

    public ChatMessage saveMessageAndBindFiles(User user, Long processId, ChatMessage message, Set<Actor> recipients,
                                               ArrayList<ChatMessageFile> files) {
        message.setProcess(processDao.get(processId));
        return chatDao.saveMessageAndBindFiles(message, files, recipients);
    }

    public void readMessage(Actor user, Long messageId) {
        chatDao.readMessage(user, messageId);
    }

    public Long getLastReadMessage(Actor user, Long processId) {
        return chatDao.getLastReadMessage(user, processId);
    }

    public Long getLastMessage(Actor user, Long processId) {
        return chatDao.getLastMessage(user, processId);
    }

    public List<Long> getActiveChatIds(Actor user) {
        List<Long> ret = chatDao.getActiveChatIds(user);
        if (ret == null) {
            ret = new ArrayList<Long>();
        }
        return ret;
    }

    public List<Long> getNewMessagesCounts(List<Long> chatsIds, Actor user) {
        return chatDao.getNewMessagesCounts(chatsIds, user);
    }

    public Long getNewMessagesCount(Actor user, Long processId) {
        return chatDao.getNewMessagesCount(user, processId);
    }

    public ChatMessage getMessage(Actor actor, Long messageId) {
        return chatDao.getMessage(messageId);
    }

    public List<MessageAddedBroadcast> getMessages(Actor user, Long processId, Long firstId, int count) {
        return chatDao.getMessages(user, processId, firstId, count);
    }

    public List<MessageAddedBroadcast> getNewMessages(Actor user, Long processId) {
        return chatDao.getNewMessages(user, processId);
    }

    public Long saveMessage(User user, Long processId, ChatMessage message, Set<Actor> recipients) {
        message.setProcess(processDao.get(processId));
        return chatDao.save(message, recipients);
    }

    public void deleteMessage(Actor actor, Long messId) {
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

    public void updateMessage(Actor actor, ChatMessage message) {
        chatDao.updateMessage(message);
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

}
