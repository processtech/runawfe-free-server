package ru.runa.wfe.service.impl;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.audit.ProcessSuspendLog;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.ITransactionListener;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * @since 4.3.0
 * @author Alex Chernyshev
 */
@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/nodeAsyncExecution"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "useDLQ", propertyValue = "false") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, SpringBeanAutowiringInterceptor.class })
public class NodeAsyncExecutionBean implements MessageListener {
    private static final Log log = LogFactory.getLog(NodeAsyncExecutionBean.class);
    private static final Map<Long, ReentrantLock> processLocks = Maps.newHashMap();
    @Autowired
    private TokenDAO tokenDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ProcessDAO processDAO;
    @Autowired
    private ProcessLogDAO processLogDAO;
    @Resource
    private MessageDrivenContext context;

    @Override
    public void onMessage(Message jmsMessage) {
        ObjectMessage message = (ObjectMessage) jmsMessage;
        if (log.isDebugEnabled()) {
            log.debug(Utils.toString(message, false));
        }
        try {
            Long processId = message.getLongProperty("processId");
            Long tokenId = message.getLongProperty("tokenId");
            handleMessage(processId, tokenId);
        } catch (Exception e) {
            log.error(message, e);
            Throwables.propagate(e);
        }
    }

    private void handleMessage(final Long processId, final Long tokenId) {
        try {
            acquireLock(processId);
            new TransactionalExecutor(context.getUserTransaction()) {

                @Override
                protected void doExecuteInTransaction() throws Exception {
                    Token token = tokenDAO.getNotNull(tokenId);
                    ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess());
                    Node node = processDefinition.getNodeNotNull(token.getNodeId());
                    try {
                        ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
                        node.execute(executionContext);
                        ProcessExecutionErrors.removeProcessError(processId, node.getNodeId());
                    } catch (Throwable th) {
                        log.error(processId + ":" + tokenId, th);
                        ProcessExecutionErrors.addProcessError(processId, node.getNodeId(), node.getName(), null, th);
                        Throwables.propagate(th);
                    }
                }
            }.executeInTransaction(true);
            for (ITransactionListener listener : TransactionListeners.get()) {
                try {
                    listener.onTransactionComplete();
                } catch (Throwable th) {
                    log.error(th);
                }
            }
            TransactionListeners.reset();
        } catch (Throwable th) {
            new TransactionalExecutor(context.getUserTransaction()) {

                @Override
                protected void doExecuteInTransaction() throws Exception {
                    Token token = tokenDAO.getNotNull(tokenId);
                    token.setExecutionStatus(ExecutionStatus.FAILED);
                    ru.runa.wfe.execution.Process process = processDAO.getNotNull(processId);
                    process.setExecutionStatus(ExecutionStatus.FAILED);
                    processLogDAO.addLog(new ProcessSuspendLog(null), process, null);
                }
            }.executeInTransaction(true);
        } finally {
            releaseLock(processId);
        }
    }

    private void acquireLock(Long processId) {
        ReentrantLock lock;
        synchronized (processLocks) {
            lock = processLocks.get(processId);
            if (lock != null) {
                log.debug("acquiring existing " + lock + " for " + processId);
            } else {
                lock = new ReentrantLock();
                log.debug("acquiring new " + lock + " for " + processId);
                processLocks.put(processId, lock);
            }
        }
        lock.lock();
        if (!processLocks.containsKey(processId)) {
            synchronized (processLocks) {
                log.debug("adding " + lock + " to map for " + processId);
                processLocks.put(processId, lock);
            }
        }
        log.debug("acquired " + lock + " for " + processId);
    }

    private void releaseLock(Long processId) {
        log.debug("releasing lock for " + processId);
        synchronized (processLocks) {
            ReentrantLock lock = processLocks.get(processId);
            if (!lock.hasQueuedThreads()) {
                log.debug("deleting " + lock + " for " + processId);
                processLocks.remove(processId);
            }
            lock.unlock();
            log.debug("released " + lock + " for " + processId);
        }
    }
}