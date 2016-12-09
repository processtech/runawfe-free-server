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

import ru.runa.wfe.audit.ReceiveMessageLog;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dto.Variables;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/bpmMessages"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "useDLQ", propertyValue = "false") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, SpringBeanAutowiringInterceptor.class })
@SuppressWarnings("unchecked")
public class ReceiveMessageBean implements MessageListener {
    private static Log log = LogFactory.getLog(ReceiveMessageBean.class);
    @Autowired
    private TokenDAO tokenDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;
    @Resource
    private MessageDrivenContext context;

    @Override
    public void onMessage(Message jmsMessage) {
        List<ReceiveMessageData> handlers = Lists.newArrayList();
        ObjectMessage message = (ObjectMessage) jmsMessage;
        String messageString = Utils.toString(message, false);
        UserTransaction transaction = context.getUserTransaction();
        try {
            log.debug("Received " + messageString);
            transaction.begin();
            List<Token> tokens = tokenDAO.findByNodeTypeAndExecutionStatusIsActive(NodeType.RECEIVE_MESSAGE);
            for (Token token : tokens) {
                try {

                    ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess().getDeployment().getId());
                    BaseMessageNode receiveMessageNode = (BaseMessageNode) token.getNodeNotNull(processDefinition);
                    ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
                    boolean suitable = true;
                    for (VariableMapping mapping : receiveMessageNode.getVariableMappings()) {
                        if (mapping.isPropertySelector()) {
                            String selectorValue = message.getStringProperty(mapping.getName());
                            String testValue = mapping.getMappedName();
                            String expectedValue;
                            if (Variables.CURRENT_PROCESS_ID_WRAPPED.equals(testValue) || "${currentInstanceId}".equals(testValue)) {
                                expectedValue = String.valueOf(token.getProcess().getId());
                            } else if (Variables.CURRENT_PROCESS_DEFINITION_NAME_WRAPPED.equals(testValue)) {
                                expectedValue = token.getProcess().getDeployment().getName();
                            } else if (Variables.CURRENT_NODE_NAME_WRAPPED.equals(testValue)) {
                                expectedValue = receiveMessageNode.getName();
                            } else if (Variables.CURRENT_NODE_ID_WRAPPED.equals(testValue)) {
                                expectedValue = receiveMessageNode.getNodeId();
                            } else {
                                Object value = ExpressionEvaluator.evaluateVariable(executionContext.getVariableProvider(), testValue);
                                expectedValue = TypeConversionUtil.convertTo(String.class, value);
                            }
                            if (!Objects.equal(expectedValue, selectorValue)) {
                                log.debug(message + " rejected in " + token + " due to diff in " + mapping.getName() + " (" + expectedValue + "!="
                                        + selectorValue + ")");
                                suitable = false;
                                break;
                            }
                        }
                    }
                    if (suitable) {
                        handlers.add(new ReceiveMessageData(executionContext, receiveMessageNode));
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
        if (handlers.size() == 0) {
            try {
                if (MessageEventType.error.name().equals(message.getStringProperty(BaseMessageNode.EVENT_TYPE))) {
                    // TODO 212 find in token hierarchy?
                    log.warn("not match tokens for errorEvent " + messageString);
                    // ProcessExecutionErrors.addProcessError(message.getStringProperty("")task, botTask, th);
                }
            } catch (JMSException e) {
                Throwables.propagate(e);
            }
            throw new MessagePostponedException(messageString);
        }
        for (ReceiveMessageData data : handlers) {
            handleMessage(data, message);
        }
    }

    private void handleMessage(final ReceiveMessageData data, final ObjectMessage message) {
        try {
            ProcessExecutionErrors.removeProcessError(data.processId, data.node.getNodeId());
            new TransactionalExecutor(context.getUserTransaction()) {

                @Override
                protected void doExecuteInTransaction() throws Exception {
                    log.info("Handling " + message + " for " + data);
                    Token token = tokenDAO.getNotNull(data.tokenId);
                    ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess().getDeployment().getId());
                    ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
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
            }.executeInTransaction(true);
        } catch (Throwable th) {
            ProcessExecutionErrors.addProcessError(data.processId, data.node.getNodeId(), data.node.getName(), null, th);
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
            return Objects.toStringHelper(getClass()).add("processId", processId).add("tokenId", tokenId).add("node", node).toString();
        }
    }

}
