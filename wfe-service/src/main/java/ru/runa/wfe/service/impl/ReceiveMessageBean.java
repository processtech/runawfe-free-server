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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ReceiveMessageLog;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/bpmMessages"),//
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
@TransactionManagement(TransactionManagementType.CONTAINER)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, SpringBeanAutowiringInterceptor.class })
@SuppressWarnings("unchecked")
public class ReceiveMessageBean implements MessageListener {
    private static Log log = LogFactory.getLog(ReceiveMessageBean.class);
    @Autowired
    private TokenDAO tokenDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ProcessLogDAO processLogDAO;
    @Resource
    private MessageDrivenContext context;

    @Override
    public void onMessage(Message jmsMessage) {
        List<ReceiveMessageData> handlers = Lists.newArrayList();
        ObjectMessage message = (ObjectMessage) jmsMessage;
        String messageString = Utils.toString(message, false);
        ErrorEventData errorEventData = null;
        try {
            log.debug("Received " + messageString);
            errorEventData = ErrorEventData.match(message);
            List<Token> tokens;
            if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
                if (SystemProperties.isProcessExecutionMessagePredefinedSelectorOnlyStrictComplianceHandling()) {
                    String messageSelector = Utils.getObjectMessageStrictSelector(message);
                    tokens = tokenDAO.findByMessageSelectorAndExecutionStatusIsActive(messageSelector);
                    log.debug("Checking " + tokens.size() + " tokens by messageSelector = " + messageSelector);
                } else {
                    Set<String> messageSelectors = Utils.getObjectMessageCombinationSelectors(message);
                    tokens = tokenDAO.findByMessageSelectorInAndExecutionStatusIsActive(messageSelectors);
                    log.debug("Checking " + tokens.size() + " tokens by messageSelectors = " + messageSelectors);
                }
            } else {
                tokens = tokenDAO.findByNodeTypeAndExecutionStatusIsActive(NodeType.RECEIVE_MESSAGE);
                log.debug("Checking " + tokens.size() + " tokens");
            }
            for (Token token : tokens) {
                try {
                    ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess().getDeployment().getId());
                    BaseMessageNode receiveMessageNode = (BaseMessageNode) token.getNodeNotNull(processDefinition);
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
                        IVariableProvider variableProvider = executionContext.getVariableProvider();
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
            if (handlers.isEmpty()) {
                if (errorEventData != null) {
                    String errorMessage = "Unexpected errorEvent in processId = " + errorEventData.processId + ", nodeId = " + errorEventData.nodeId;
                    log.error(errorMessage);
                    Errors.addSystemError(new InternalApplicationException(errorMessage));
                } else {
                    log.debug("Rejecting " + messageString);
                    context.setRollbackOnly();
                }
            } else {
                log.debug("Handling " + messageString);
                for (ReceiveMessageData data : handlers) {
                    handleMessage(data, message);
                }
            }
        } catch (Exception e) {
            log.error("", e);
            context.setRollbackOnly();
        }
    }

    private void handleMessage(final ReceiveMessageData data, final ObjectMessage message) throws JMSException {
        log.debug("Handling " + message + " for " + data);
        Token token = tokenDAO.getNotNull(data.tokenId);
        if (!Objects.equal(token.getNodeId(), data.node.getNodeId())) {
            throw new InternalApplicationException(token + " not in " + data.node.getNodeId());
        }
        ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess().getDeployment().getId());
        ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
        executionContext.activateTokenIfHasPreviousError();
        executionContext.addLog(new ReceiveMessageLog(data.node, Utils.toString(message, true)));
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
            return Objects.toStringHelper(getClass()).add("processId", processId).add("tokenId", tokenId).add("node", node).toString();
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
