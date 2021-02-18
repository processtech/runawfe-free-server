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
import ru.runa.wfe.chat.dao.ChatFileIo;
import ru.runa.wfe.chat.dao.ChatMessageDao;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.mapper.ChatMessageFileMapper;
import ru.runa.wfe.chat.utils.DtoConverters;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class ChatLogic extends WfCommonLogic {
    private final Properties properties = ClassLoaderUtil.getProperties("chat.email.properties", false);
    @Autowired
    private ChatMessageDao messageDao;
    @Autowired
    private ChatFileLogic fileLogic;
    @Autowired
    private DtoConverters converter;
    @Autowired
    private ChatMessageFileMapper fileMapper;
    @Autowired
    private ChatFileIo fileIo;
    @Autowired
    private MessageTransactionWrapper messageTransactionWrapper;

    public MessageAddedBroadcast saveMessage(User user, Long processId, ChatMessage message, Set<Actor> recipients) {
        final ChatMessage savedMessage = messageTransactionWrapper.save(message, recipients, processId);
        return converter.convertChatMessageToAddedMessageBroadcast(savedMessage);
    }

    public MessageAddedBroadcast saveMessage(User user, Long processId, ChatMessage message, Set<Actor> recipients, List<ChatMessageFileDto> files) {
        final List<ChatMessageFile> savedFiles = fileIo.save(files);
        try {
            final ChatMessage savedMessage = messageTransactionWrapper.save(message, recipients, savedFiles, processId);
            final MessageAddedBroadcast broadcast = converter.convertChatMessageToAddedMessageBroadcast(savedMessage);
            broadcast.setFiles(fileMapper.toDetailDto(savedFiles));
            return broadcast;
        } catch (Exception exception) {
            fileIo.delete(savedFiles);
            throw exception;
        }
    }

    public void readMessage(User user, Long messageId) {
        messageDao.readMessage(user.getActor(), messageId);
    }

    public Long getLastReadMessage(User user, Long processId) {
        return messageDao.getLastReadMessage(user.getActor(), processId);
    }

    public List<Long> getActiveChatIds(User user) {
        List<Long> ret = messageDao.getActiveChatIds(user.getActor());
        if (ret == null) {
            ret = new ArrayList<>();
        }
        return ret;
    }

    public List<Long> getNewMessagesCounts(User user, List<Long> chatsIds) {
        return messageDao.getNewMessagesCounts(chatsIds, user.getActor());
    }

    public ChatMessage getMessageById(User user, Long messageId) {
        return messageDao.get(messageId);
    }

    public List<MessageAddedBroadcast> getMessages(User user, Long processId, Long firstId, int count) {
        List<ChatMessage> messages = messageDao.getMessages(user.getActor(), processId, firstId, count);
        return toMessageAddedBroadcast(user, messages);
    }

    public List<MessageAddedBroadcast> getNewMessages(User user, Long processId) {
        List<ChatMessage> messages = messageDao.getNewMessages(user.getActor(), processId);
        return toMessageAddedBroadcast(user, messages);
    }

    public void deleteMessage(User user, Long messageId) {
        fileIo.delete(messageTransactionWrapper.delete(user, messageId));
    }

    public void updateMessage(User user, ChatMessage message) {
        if (!message.getCreateActor().equals(user.getActor())) {
            throw new AuthorizationException("Allowed for author only");
        }
        messageDao.update(message);
    }

    public void sendNotifications(User user, ChatMessage chatMessage, Collection<Executor> executors) {
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

    private List<MessageAddedBroadcast> toMessageAddedBroadcast(User user, List<ChatMessage> messages) {
        List<MessageAddedBroadcast> result = new ArrayList<>(messages.size());
        for (ChatMessage message : messages) {
            MessageAddedBroadcast broadcast = converter.convertChatMessageToAddedMessageBroadcast(message);
            broadcast.setFiles(fileMapper.toDetailDto(fileLogic.getByMessage(user, message)));
            result.add(broadcast);
        }
        return result;
    }

}
