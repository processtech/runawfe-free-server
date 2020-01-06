package ru.runa.wfe.chat.logic;

import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.user.Actor;

public class ChatLogic extends WfCommonLogic {

    private Properties properties = ClassLoaderUtil.getProperties("chat.properties", true);

    public ChatsUserInfo getUserInfo(Actor actor, int chatId) {
        return chatDao.getUserInfo(actor, chatId);
    }

    public long getNewMessagesCount(long lastMessageId, int chatId) {
        return chatDao.getNewMessagesCount(lastMessageId, chatId);
    }

    public void updateUserInfo(Actor actor, int chatId, long lastMessageId) {
        chatDao.updateUserInfo(actor, chatId, lastMessageId);
    }

    public List<ChatMessage> getMessages(int chatId) {
        return chatDao.getAll(chatId);
    }

    public ChatMessage getMessage(long messageId) {
        return chatDao.getMessage(messageId);
    }

    public List<ChatMessage> getMessages(int chatId, Long firstId, int count) {
        return chatDao.getMessages(chatId, firstId, count);
    }

    public List<ChatMessage> getFirstMessages(int chatId, int count) {
        return chatDao.getFirstMessages(chatId, count);
    }

    public List<ChatMessage> getNewMessages(int chatId, Long lastId) {
        return chatDao.getNewMessages(chatId, lastId);
    }

    public long setMessage(int chatId, ChatMessage message) {
        return chatDao.save(message);
    }

    public long getAllMessagesCount(int chatId) {
        return chatDao.getMessagesCount(chatId);
    }

    public void deleteMessage(long messId) {
        chatDao.deleteMessageFiles(messId);
        chatDao.deleteMessage(messId);
    }

    public List<Integer> getAllConnectedChatId(int chatId) {
        return chatDao.getAllConnectedChatId(chatId);
    }

    public List<ChatMessageFile> getMessageFiles(ChatMessage message) {
        return chatDao.getMessageFiles(message);
    }

    public ChatMessageFile saveFile(ChatMessageFile file) {
        return chatDao.saveFile(file);
    }

    public ChatMessageFile getFile(long fileId) {
        return chatDao.getFile(fileId);
    }

    public void updateMessage(ChatMessage message) {
        chatDao.updateMessage(message);
    }

    public boolean canEditMessage(Actor user) {
        return true;
    }

    public List<Actor> getAllUsersNames(int chatId) {
        return chatDao.getAllUsersNames(chatId);
    }

    public boolean sendMessageToEmail(String title, String message, String Emaile) {
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
