package ru.runa.wfe.service.impl;

import com.google.common.base.Throwables;
import java.io.IOException;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;

/**
 * Non-blocking and transactional email sending.
 *
 * @since 4.2
 */
@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/email"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class })
@CommonsLog
public class EmailSenderBean implements MessageListener {

    @Override
    public void onMessage(Message jmsMessage) {
        ObjectMessage message = (ObjectMessage) jmsMessage;
        EmailConfig config;
        try {
            config = (EmailConfig) message.getObject();
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
        try {
            EmailUtils.sendMessage(config);
            return;
        } catch (IOException | MessagingException e) {
            log.warn(config);
            log.error("unable to send email: " + e);
        } catch (Exception e) {
            log.warn(config);
            log.error("unable to send email", e);
            if (!config.isThrowErrorOnFailure()) {
                return;
            }
        }
        if (SystemProperties.isEmailGuaranteedDeliveryEnabled()) {
            throw new MessagePostponedException("email guaranteed delivery requested");
        }
    }

}
