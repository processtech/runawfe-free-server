package ru.runa.wfe.service.impl;

import com.google.common.base.Throwables;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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
import javax.transaction.UserTransaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListener;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;

/**
 * @since 4.3.0
 * @author Alex Chernyshev
 */
@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/nodeAsyncExecution"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, SpringBeanAutowiringInterceptor.class })
public class NodeAsyncExecutionBean implements MessageListener {
    private static final Log log = LogFactory.getLog(NodeAsyncExecutionBean.class);
    @Autowired
    private TokenDao tokenDao;
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
                protected void doExecuteInTransaction() throws Exception {
                    Token token = tokenDao.getNotNull(tokenId);
                    if (token.getProcess().hasEnded()) {
                        log.debug("Ignored execution in ended " + token.getProcess());
                        return;
                    }
                    if (!Objects.equals(nodeId, token.getNodeId())) {
                        throw new InternalApplicationException(token + " expected to be in node " + nodeId);
                    }
                    if (token.getVersion() >= SystemProperties.getTokenMaximumLength()) {
                        throw new InternalApplicationException("Maximum token length " + SystemProperties.getTokenMaximumLength()
                                + " has been reached");
                    }
                    ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess());
                    Node node = processDefinition.getNodeNotNull(token.getNodeId());
                    try {
                        ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
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
            boolean needReprocessing = failProcessExecution(context.getUserTransaction(), tokenId, nodeId, th);
            if (needReprocessing) {
                throw new MessagePostponedException("process id = " + processId + ", token id = " + tokenId);
            }
        }
    }

    private boolean failProcessExecution(UserTransaction transaction, final Long tokenId, String nodeId, final Throwable throwable) {
        final AtomicBoolean needReprocessing = new AtomicBoolean(false);
        new TransactionalExecutor(transaction) {

            @Override
            protected void doExecuteInTransaction() throws Exception {
                Token token = ApplicationContextFactory.getTokenDAO().getNotNull(tokenId);
                if (token.hasEnded()) {
                    log.debug("Ignored fail processs execution in ended " + token);
                    return;
                }
                if (!Objects.equals(nodeId, token.getNodeId())) {
                    log.debug("Ignored fail process execution: " + token + " expected to be in node " + nodeId);
                    return;
                }
                boolean stateChanged = token.fail(Throwables.getRootCause(throwable));
                if (stateChanged && token.getProcess().getExecutionStatus() == ExecutionStatus.ACTIVE) {
                    token.getProcess().setExecutionStatus(ExecutionStatus.FAILED);
                    needReprocessing.set(true);
                }
            }
        }.executeInTransaction(true);
        return needReprocessing.get();
    }

}