package ru.runa.wfe.chat.sender;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.user.Actor;

/**
 * Created on 22.04.2021
 *
 * @author Sergey Inyakin
 * @since 2148
 */
@CommonsLog
public class ChatEmailNotificationConfigurator {
    EmailConfig config;
    List<String> emailsToSend;
    String emails;

    public ChatEmailNotificationConfigurator(byte[] configBytes) {
        this.config = EmailConfigParser.parse(configBytes);
    }

    public void setAddressesByActor(Actor actor) {
        emailsToSend = EmailUtils.getEmails(actor);
        emails = EmailUtils.concatenateEmails(emailsToSend);
        config.getHeaderProperties().put(EmailConfig.HEADER_TO, emails);
    }

    public boolean isAddressesEmpty() {
        return emailsToSend.isEmpty();
    }

    public void setSubject(int newMessagesAmount) {
        config.getHeaderProperties().put("Subject", "Количество непрочитанных сообщений: " + newMessagesAmount);
    }

    public void sendMessage(String message) {
        if (message.isEmpty()) {
            return;
        }
        try {
            config.setMessage(message);
            EmailUtils.sendMessage(config);
        } catch (Exception e) {
            log.error("Email notification to: " + emails + " send error: " + e);
        }
    }


}
