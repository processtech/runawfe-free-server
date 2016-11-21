/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.service.impl;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;

import com.google.common.base.Throwables;

/**
 * Non-blocking and transactional email sending.
 *
 * @since 4.2
 */
@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/email"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class })
public class EmailSenderBean implements MessageListener {
    private static Log log = LogFactory.getLog(EmailSenderBean.class);

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
        } catch (IOException e) {
            log.warn(config);
            log.error("unable to send email: " + e);
        } catch (MessagingException e) {
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
