package ru.runa.wfe.service.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentReceiveMessageLog;
import ru.runa.wfe.commons.SystemErrors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Signal;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.dao.SignalDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.BaseReceiveMessageNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
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
    @Autowired
    private TransactionalExecutor transactionalExecutor;
    @Autowired
    private SignalDao signalDao;

    @Override
    public void onMessage(Message jmsMessage) {
        List<ReceiveMessageData> handlers = Lists.newArrayList();
        ObjectMessage message = (ObjectMessage) jmsMessage;
        String messageString = Utils.toString(message, false);
        log.debug("Received " + messageString);
        ErrorEventData errorEventData = ErrorEventData.match(message);
        try {
            transactionalExecutor.execute(() -> {
                List<CurrentToken> tokens;
                if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
                    Map<String, String> routingData = getRoutingData(message);
                    tokens = executionLogic.findTokensForMessageSelector(routingData);
                    log.debug("Checking " + tokens.size() + " tokens by routingData = " + routingData);
                } else {
                    tokens = currentTokenDao.findByNodeTypeInActiveProcesses(NodeType.RECEIVE_MESSAGE);
                    log.debug("Checking " + tokens.size() + " tokens");
                }
                for (CurrentToken token : tokens) {
                    try {
                        ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(token.getProcess());
                        BaseReceiveMessageNode receiveMessageNode = (BaseReceiveMessageNode) token.getNodeNotNull(parsedProcessDefinition);
                        ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, token);
                        if (errorEventData != null) {
                            if (receiveMessageNode.getEventType() == MessageEventType.error
                                    && receiveMessageNode.getParentElement() instanceof Node) {
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
            });
        } catch (Exception e) {
            log.error("", e);
            Throwables.propagate(e);
        }
        if (handlers.isEmpty()) {
            if (errorEventData != null) {
                String errorMessage = "Unexpected errorEvent in processId = " + errorEventData.processId + ", nodeId = " + errorEventData.nodeId;
                log.error(errorMessage);
                SystemErrors.addError(new InternalApplicationException(errorMessage));
            } else {
                try {
                    transactionalExecutor.execute(() -> {
                        Date createDate = message.getJMSTimestamp() != 0 ? new Date(message.getJMSTimestamp()) : new Date();
                        Date expiryDate = null;
                        if (message.propertyExists(BaseMessageNode.EXPIRATION_PROPERTY)) {
                            expiryDate = new Date(message.getLongProperty(BaseMessageNode.EXPIRATION_PROPERTY));
                        }
                        if (expiryDate == null || expiryDate.after(new Date())) {
                            Signal signal = new Signal(createDate, getRoutingData(message), (Map<String, Object>) message.getObject(), expiryDate);
                            log.debug("Rejecting message request " + messageString + ", persisting to " + signal);
                            signalDao.create(signal);
                        } else {
                            log.debug("Rejecting message request " + messageString + ", already expired");
                        }
                    });
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
        }
        for (ReceiveMessageData data : handlers) {
            handleMessage(data, message);
        }
    }

    private Map<String, String> getRoutingData(ObjectMessage message) throws JMSException {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            if (propertyName.startsWith("JMS")) {
                continue;
            }
            if (BaseMessageNode.EXPIRATION_PROPERTY.equals(propertyName)) {
                continue;
            }
            map.put(propertyName, message.getStringProperty(propertyName));
        }
        return map;
    }

    private void handleMessage(final ReceiveMessageData data, final ObjectMessage message) {
        try {
            transactionalExecutor.execute(() -> {
                log.info("Handling " + message + " for " + data);
                CurrentToken token = currentTokenDao.getNotNull(data.tokenId);
                if (!Objects.equal(token.getNodeId(), data.node.getNodeId())) {
                    throw new InternalApplicationException(token + " not in " + data.node.getNodeId());
                }
                ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(token.getProcess());
                ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, token);
                executionContext.activateTokenIfHasPreviousError();
                executionContext.addLog(new CurrentReceiveMessageLog(data.node, Utils.toString(message, true)));
                Map<String, Object> map = (Map<String, Object>) message.getObject();
                data.node.leave(executionContext, map);
                executionLogic.removeTokenError(data.tokenId);
            });
        } catch (final Throwable th) {
            transactionalExecutor.execute(() -> {
                executionLogic.failToken(data.tokenId, th);
            });
            Throwables.propagate(th);
        }
    }

    private static class ReceiveMessageData {
        private Long processId;
        private Long tokenId;
        private BaseReceiveMessageNode node;

        public ReceiveMessageData(ExecutionContext executionContext, BaseReceiveMessageNode node) {
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

        @SneakyThrows
        public static ErrorEventData match(ObjectMessage message) {
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
