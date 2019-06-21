package ru.runa.wfe.service.impl;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TransactionListener;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;

/**
 * @since 4.3.0
 * @author Alex Chernyshev
 */
@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/nodeAsyncExecution"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "useDLQ", propertyValue = "false") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, SpringBeanAutowiringInterceptor.class })
@CommonsLog
public class NodeAsyncExecutionBean implements MessageListener {
    @Autowired
    private CurrentTokenDao currentTokenDao;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Resource
    private MessageDrivenContext context;

    @Override
    public void onMessage(Message jmsMessage) {
        try {
            ObjectMessage message = (ObjectMessage) jmsMessage;
            handleMessage(message);
        } catch (Exception e) {
            log.error(jmsMessage, e);
            Throwables.propagateIfInstanceOf(e, MessagePostponedException.class);
            throw new MessagePostponedException(e.getMessage());
        }
    }

    private void handleMessage(final ObjectMessage message) throws JMSException {
        final Long processId = message.getLongProperty("processId");
        final Long tokenId = message.getLongProperty("tokenId");
        final String nodeId = message.getStringProperty("nodeId");
        log.debug("handling node async execution request: {processId=" + processId + ", tokenId=" + tokenId + ", nodeId=" + nodeId + "}");
        final boolean retry = message.getBooleanProperty("retry");
        if (message.getJMSRedelivered() && !retry) {
            log.debug("rejected due to redelivering");
            return;
        }
        try {
            new TransactionalExecutor(context.getUserTransaction()) {

                @Override
                protected void doExecuteInTransaction() {
                    CurrentToken token = currentTokenDao.getNotNull(tokenId);
                    if (token.getProcess().hasEnded()) {
                        log.debug("Ignored execution in ended " + token.getProcess());
                        return;
                    }
                    if (!Objects.equal(nodeId, token.getNodeId())) {
                        throw new InternalApplicationException(token + " expected to be in node " + nodeId);
                    }
                    ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(token.getProcess());
                    Node node = parsedProcessDefinition.getNodeNotNull(token.getNodeId());
                    try {
                        ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, token);
                        node.handle(executionContext);
                    } catch (Throwable th) {
                        log.error(processId + ":" + tokenId, th);
                        Throwables.propagate(th);
                    }
                }
            }.executeInTransaction(true);
            for (TransactionListener listener : TransactionListeners.get()) {
                try {
                    listener.onTransactionComplete(context.getUserTransaction());
                } catch (Throwable th) {
                    log.error(th);
                }
            }
            TransactionListeners.reset();
        } catch (final Throwable th) {
            // TODO does not work in case of timeout in handling transaction
            // ARJUNA016051: thread is already associated with a transaction!
            executionLogic.failProcessExecution(context.getUserTransaction(), tokenId, th);
            throw new MessagePostponedException("process id = " + processId + ", token id = " + tokenId);
        }
    }

}