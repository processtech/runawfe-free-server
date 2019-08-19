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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ru.runa.wfe.audit.ReceiveMessageLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Signal;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.SignalDao;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.BaseReceiveMessageNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
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
public class ReceiveMessageBean implements MessageListener {
    private static Log log = LogFactory.getLog(ReceiveMessageBean.class);
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private TokenDao tokenDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ProcessLogDao processLogDao;
    @Resource
    private MessageDrivenContext context;
    @Autowired
    private SignalDao signalDao;

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
            List<Token> tokens;
            if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
                Map<String, String> routingData = getRoutingData(message);
                tokens = executionLogic.findTokensForMessageSelector(routingData);
                log.debug("Checking " + tokens.size() + " tokens by routingData = " + routingData);
            } else {
                tokens = tokenDao.findByNodeTypeAndExecutionStatusIsActive(NodeType.RECEIVE_MESSAGE);
                log.debug("Checking " + tokens.size() + " tokens");
            }
            for (Token token : tokens) {
                try {
                    ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess().getDeployment().getId());
                    BaseReceiveMessageNode receiveMessageNode = (BaseReceiveMessageNode) token.getNodeNotNull(processDefinition);
                    ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
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
                try {
                    transaction.begin();
                    Signal signal = new Signal(message);
                    log.debug("Rejecting message request " + messageString + ", persisting to " + signal);
                    signalDao.create(signal);
                    transaction.commit();
                } catch (Exception e) {
                    Utils.rollbackTransaction(transaction);
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
            if (!propertyName.startsWith("JMS")) {
                map.put(propertyName, message.getStringProperty(propertyName));
            }
        }
        return map;
    }

    private void handleMessage(final ReceiveMessageData data, final ObjectMessage message) {
        try {
            new TransactionalExecutor(context.getUserTransaction()) {

                @Override
                protected void doExecuteInTransaction() throws Exception {
                    log.info("Handling " + message + " for " + data);
                    Token token = tokenDao.getNotNull(data.tokenId);
                    if (!Objects.equal(token.getNodeId(), data.node.getNodeId())) {
                        throw new InternalApplicationException(token + " not in " + data.node.getNodeId());
                    }
                    ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess().getDeployment().getId());
                    ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
                    executionContext.activateTokenIfHasPreviousError();
                    executionContext.addLog(new ReceiveMessageLog(data.node, Utils.toString(message, true)));
                    Map<String, Object> map = (Map<String, Object>) message.getObject();
                    data.node.leave(executionContext, map);
                }
            }.executeInTransaction(true);
        } catch (final Throwable th) {
            Utils.failProcessExecution(context.getUserTransaction(), data.tokenId, th);
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
