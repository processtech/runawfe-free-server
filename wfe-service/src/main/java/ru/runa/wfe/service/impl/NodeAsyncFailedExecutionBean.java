package ru.runa.wfe.service.impl;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/nodeAsyncFailedExecution"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
@TransactionManagement(TransactionManagementType.CONTAINER)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, SpringBeanAutowiringInterceptor.class })
public class NodeAsyncFailedExecutionBean implements MessageListener {
    private static final Log log = LogFactory.getLog(NodeAsyncFailedExecutionBean.class);
    @Resource
    private MessageDrivenContext context;

    @Override
    public void onMessage(Message jmsMessage) {
        try {
            ObjectMessage message = (ObjectMessage) jmsMessage;
            final Long tokenId = message.getLongProperty("tokenId");
            Utils.failProcessExecution(tokenId, new Exception("DLQ"));
        } catch (Exception e) {
            log.error(jmsMessage, e);
            context.setRollbackOnly();
        }
    }

}