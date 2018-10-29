package ru.runa.wfe.service.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentReceiveMessageLog;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.VariableProvider;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/bpmMessages"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "useDLQ", propertyValue = "false") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, SpringBeanAutowiringInterceptor.class })
@SuppressWarnings("unchecked")
@CommonsLog
public class ReceiveMessageBean implements MessageListener {
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private CurrentTokenDao currentTokenDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Resource
    private MessageDrivenContext context;

    @Override
    public void onMessage(Message jmsMessage) {
        List<ReceiveMessageData> handlers = Lists.newArrayList();
        ObjectMessage message = (ObjectMessage) jmsMessage;
        String messageString = Utils.toString(message, false);
        UserTransaction transaction = context.getUserTransaction();
        ErrorEventData errorEventData = null;
        try {
            log.debug("Received " + messageString);
            errorEventData = ErrorEventData.match(message);
            transaction.begin();
            List<CurrentToken> tokens;
            if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
                if (SystemProperties.isProcessExecutionMessagePredefinedSelectorOnlyStrictComplianceHandling()) {
                    String messageSelector = Utils.getObjectMessageStrictSelector(message);
                    tokens = currentTokenDao.findByMessageSelectorAndExecutionStatusIsActive(messageSelector);
                    log.debug("Checking " + tokens.size() + " tokens by messageSelector = " + messageSelector);
                } else {
                    Set<String> messageSelectors = Utils.getObjectMessageCombinationSelectors(message);
                    tokens = currentTokenDao.findByMessageSelectorInAndExecutionStatusIsActive(messageSelectors);
                    log.debug("Checking " + tokens.size() + " tokens by messageSelectors = " + messageSelectors);
                }
            } else {
                tokens = currentTokenDao.findByNodeTypeAndExecutionStatusIsActive(NodeType.RECEIVE_MESSAGE);
                log.debug("Checking " + tokens.size() + " tokens");
            }
            for (CurrentToken token : tokens) {
                try {
                    ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(
                            token.getProcess().getDefinitionVersion().getId()
                    );
                    BaseMessageNode receiveMessageNode = (BaseMessageNode) token.getNodeNotNull(parsedProcessDefinition);
                    ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, token);
                    if (errorEventData != null) {
                        if (receiveMessageNode.getEventType() == MessageEventType.error && receiveMessageNode.getParentElement() instanceof Node) {
                            Long processId = token.getProcess().getId();
                            String nodeId = ((Node) receiveMessageNode.getParentElement()).getNodeId();
                            if (processId.equals(errorEventData.processId) && nodeId.equals(errorEventData.nodeId)) {
                                handlers.add(new ReceiveMessageData(executionContext, receiveMessageNode));
                                break;
                            }
                        }
                    } else {
                        boolean suitable = true;
                        VariableProvider variableProvider = executionContext.getVariableProvider();
                        for (VariableMapping mapping : receiveMessageNode.getVariableMappings()) {
                            if (mapping.isPropertySelector()) {
                                String selectorValue = message.getStringProperty(mapping.getName());
                                String expectedValue = Utils.getMessageSelectorValue(variableProvider, receiveMessageNode, mapping);
                                if (!Objects.equal(expectedValue, selectorValue)) {
                                    log.debug(message + " rejected in " + token + " due to diff in " + mapping.getName() + " (" + expectedValue
                                            + "!=" + selectorValue + ")");
                                    suitable = false;
                                    break;

                                }
                            }
                        }
                        if (suitable) {
                            handlers.add(new ReceiveMessageData(executionContext, receiveMessageNode));
                        }
                    }
                } catch (Exception e) {
                    log.error("Unable to handle " + token, e);
                }
            }
            transaction.commit();
        } catch (Exception e) {
            log.error("", e);
            Utils.rollbackTransaction(transaction);
            Throwables.propagate(e);
        }
        if (handlers.isEmpty()) {
            if (errorEventData != null) {
                String errorMessage = "Unexpected errorEvent in processId = " + errorEventData.processId + ", nodeId = " + errorEventData.nodeId;
                log.error(errorMessage);
                Errors.addSystemError(new InternalApplicationException(errorMessage));
            } else {
                throw new MessagePostponedException(messageString);
            }
        }
        for (ReceiveMessageData data : handlers) {
            handleMessage(data, message);
        }
    }

    private void handleMessage(final ReceiveMessageData data, final ObjectMessage message) {
        try {
            new TransactionalExecutor(context.getUserTransaction()) {

                @Override
                protected void doExecuteInTransaction() throws Exception {
                    log.info("Handling " + message + " for " + data);
                    CurrentToken token = currentTokenDao.getNotNull(data.tokenId);
                    if (!Objects.equal(token.getNodeId(), data.node.getNodeId())) {
                        throw new InternalApplicationException(token + " not in " + data.node.getNodeId());
                    }
                    ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(
                            token.getProcess().getDefinitionVersion().getId()
                    );
                    ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, token);
                    executionContext.activateTokenIfHasPreviousError();
                    executionContext.addLog(new CurrentReceiveMessageLog(data.node, Utils.toString(message, true)));
                    Map<String, Object> map = (Map<String, Object>) message.getObject();
                    for (VariableMapping variableMapping : data.node.getVariableMappings()) {
                        if (!variableMapping.isPropertySelector()) {
                            if (map.containsKey(variableMapping.getMappedName())) {
                                Object value = map.get(variableMapping.getMappedName());
                                executionContext.setVariableValue(variableMapping.getName(), value);
                            } else {
                                log.warn("message does not contain value for '" + variableMapping.getMappedName() + "'");
                            }
                        }
                    }
                    data.node.leave(executionContext);
                }
            }.executeInTransaction(true);
        } catch (final Throwable th) {
            executionLogic.failProcessExecution(context.getUserTransaction(), data.tokenId, th);
            Throwables.propagate(th);
        }
    }

    private static class ReceiveMessageData {
        private Long processId;
        private Long tokenId;
        private BaseMessageNode node;

        public ReceiveMessageData(ExecutionContext executionContext, BaseMessageNode node) {
            this.processId = executionContext.getProcess().getId();
            this.tokenId = executionContext.getToken().getId();
            this.node = node;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(getClass()).add("processId", processId).add("tokenId", tokenId).add("node", node).toString();
        }
    }

    private static class ErrorEventData {
        private Long processId;
        private String nodeId;

        public static ErrorEventData match(ObjectMessage message) throws JMSException {
            if (MessageEventType.error.name().equals(message.getStringProperty(BaseMessageNode.EVENT_TYPE))) {
                ErrorEventData data = new ErrorEventData();
                data.processId = Long.valueOf(message.getStringProperty(BaseMessageNode.ERROR_EVENT_PROCESS_ID));
                data.nodeId = message.getStringProperty(BaseMessageNode.ERROR_EVENT_NODE_ID);
                return data;
            }
            return null;
        }
    }
}
