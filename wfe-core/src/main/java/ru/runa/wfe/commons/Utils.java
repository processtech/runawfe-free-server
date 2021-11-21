package ru.runa.wfe.commons;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.Variables;

public class Utils {
    public static final String CATEGORY_DELIMITER = "/";
    private static Log log = LogFactory.getLog(Utils.class);
    private static volatile InitialContext initialContext;
    private static TransactionManager transactionManager;
    private static ConnectionFactory connectionFactory;
    private static Queue bpmMessageQueue;
    private static Queue emailQueue;
    private static Queue nodeAsyncExecutionQueue;
    private static final String MESSAGE_SELECTOR_DELIMITER = ",";
    private static final String MESSAGE_SELECTOR_VALUE_DELIMITER = "=";

    private static InitialContext getInitialContext() throws NamingException {
        if (initialContext == null) {
            initialContext = new InitialContext();
        }
        return initialContext;
    }

    public static synchronized UserTransaction getUserTransaction() {
        String jndiName = DatabaseProperties.getUserTransactionJndiName();
        try {
            return (UserTransaction) getInitialContext().lookup(jndiName);
        } catch (NamingException e) {
            throw new InternalApplicationException("Unable to find UserTransaction by name '" + jndiName + "'", e);
        }
    }

    public static synchronized TransactionManager getTransactionManager() {
        if (transactionManager != null) {
            return transactionManager;
        }
        String jndiName = "java:/TransactionManager";
        try {
            transactionManager = (TransactionManager) getInitialContext().lookup(jndiName);
            return transactionManager;
        } catch (NamingException e) {
            throw new InternalApplicationException("Unable to find TransactionManager by name '" + jndiName + "'", e);
        }
    }

    public static synchronized Transaction getTransaction() {
        try {
            return getTransactionManager().getTransaction();
        } catch (SystemException e) {
            throw new InternalApplicationException("Unexpected exception while getting current transaction", e);
        }
    }

    private static synchronized void init() throws JMSException, NamingException {
        if (connectionFactory == null) {
            String connectionFactoryJndiName = SystemProperties.getResources().getStringProperty("jndi.jms.connection.factory", "java:/JmsXA");
            try {
                connectionFactory = (ConnectionFactory) getInitialContext().lookup(connectionFactoryJndiName);
            } catch (Exception e) {
                throw new InternalApplicationException("Unable to find JMS ConnectionFactory by name '" + connectionFactoryJndiName, e);
            }
            bpmMessageQueue = (Queue) getInitialContext().lookup("queue/bpmMessages");
            emailQueue = (Queue) getInitialContext().lookup("queue/email");
            nodeAsyncExecutionQueue = (Queue) getInitialContext().lookup("queue/nodeAsyncExecution");
        }
    }

    private static void releaseJmsSession(Connection connection, Session session, MessageProducer sender) {
        if (sender != null) {
            try {
                sender.close();
            } catch (Exception ignore) {
            }
        }
        if (session != null) {
            try {
                session.close();
            } catch (Exception ignore) {
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignore) {
            }
        }
    }

    public static ObjectMessage sendBpmnMessage(List<VariableMapping> data, VariableProvider variableProvider, long ttlInMilliSeconds) {
        HashMap<String, String> routingData = new HashMap<>();
        HashMap<String, Object> payloadData = new HashMap<>();
        for (VariableMapping variableMapping : data) {
            if (variableMapping.isPropertySelector()) {
                Object value = ExpressionEvaluator.evaluateVariableNotNull(variableProvider, variableMapping.getMappedName());
                String stringValue = TypeConversionUtil.convertTo(String.class, value);
                routingData.put(variableMapping.getName(), stringValue);
            } else {
                payloadData.put(variableMapping.getMappedName(), variableProvider.getValue(variableMapping.getName()));
            }
        }
        return sendBpmnMessage(routingData, payloadData, ttlInMilliSeconds);
    }

    public static ObjectMessage sendBpmnMessage(Map<String, String> routingData, Map<String, ?> payloadData, long ttlInMilliSeconds) {
        if (routingData.isEmpty()) {
            throw new InternalApplicationException("Routing data is required");
        }
        Connection connection = null;
        Session session = null;
        MessageProducer sender = null;
        try {
            init();
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            sender = session.createProducer(bpmMessageQueue);
            ObjectMessage message = session.createObjectMessage(new HashMap<>(payloadData));
            for (Map.Entry<String, String> entry : routingData.entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }
            if (ttlInMilliSeconds >= 0) {
                message.setLongProperty(BaseMessageNode.EXPIRATION_PROPERTY, System.currentTimeMillis() + ttlInMilliSeconds);
            }
            sender.send(message);
            sender.close();
            log.info("message sent: " + toString(message, false));
            return message;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            releaseJmsSession(connection, session, sender);
        }
    }

    public static void sendBpmnErrorMessage(Long processId, String nodeId, Throwable throwable) {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(BaseMessageNode.EVENT_TYPE, MessageEventType.error.name());
        variables.put(BaseMessageNode.ERROR_EVENT_MESSAGE, throwable.getMessage());
        variables.put(BaseMessageNode.ERROR_EVENT_PROCESS_ID, processId);
        variables.put(BaseMessageNode.ERROR_EVENT_NODE_ID, nodeId);
        MapVariableProvider variableProvider = new MapVariableProvider(variables);
        List<VariableMapping> variableMappings = Lists.newArrayList();
        variableMappings
                .add(new VariableMapping(BaseMessageNode.EVENT_TYPE, Variables.wrap(BaseMessageNode.EVENT_TYPE), VariableMapping.USAGE_SELECTOR));
        variableMappings.add(new VariableMapping(BaseMessageNode.ERROR_EVENT_PROCESS_ID, Variables.wrap(BaseMessageNode.ERROR_EVENT_PROCESS_ID),
                VariableMapping.USAGE_SELECTOR));
        variableMappings.add(new VariableMapping(BaseMessageNode.ERROR_EVENT_NODE_ID, Variables.wrap(BaseMessageNode.ERROR_EVENT_NODE_ID),
                VariableMapping.USAGE_SELECTOR));
        variableMappings
                .add(new VariableMapping(BaseMessageNode.ERROR_EVENT_MESSAGE, BaseMessageNode.ERROR_EVENT_MESSAGE, VariableMapping.USAGE_READ));
        Utils.sendBpmnMessage(variableMappings, variableProvider, 0);
    }

    public static String getMessageSelectorValue(VariableProvider variableProvider, BaseMessageNode messageNode, VariableMapping mapping) {
        String testValue = mapping.getMappedName();
        if (Variables.CURRENT_PROCESS_ID_WRAPPED.equals(testValue) || "${currentInstanceId}".equals(testValue)) {
            return String.valueOf(variableProvider.getProcessId());
        } else if (Variables.CURRENT_PROCESS_DEFINITION_NAME_WRAPPED.equals(testValue)) {
            return variableProvider.getProcessDefinitionName();
        } else if (Variables.CURRENT_NODE_NAME_WRAPPED.equals(testValue)) {
            return messageNode.getName();
        } else if (Variables.CURRENT_NODE_ID_WRAPPED.equals(testValue)) {
            return messageNode.getNodeId();
        } else {
            Object value = ExpressionEvaluator.evaluateVariable(variableProvider, testValue);
            return TypeConversionUtil.convertTo(String.class, value);
        }
    }

    public static String getReceiveMessageNodeSelector(VariableProvider variableProvider, BaseMessageNode messageNode) {
        List<String> selectors = Lists.newArrayList();
        if (messageNode.getEventType() == MessageEventType.error && messageNode.getParentElement() instanceof Node) {
            selectors.add(BaseMessageNode.EVENT_TYPE + MESSAGE_SELECTOR_VALUE_DELIMITER + MessageEventType.error.name());
            selectors
                    .add(BaseMessageNode.ERROR_EVENT_PROCESS_ID + MESSAGE_SELECTOR_VALUE_DELIMITER + String.valueOf(variableProvider.getProcessId()));
            selectors.add(
                    BaseMessageNode.ERROR_EVENT_NODE_ID + MESSAGE_SELECTOR_VALUE_DELIMITER + ((Node) messageNode.getParentElement()).getNodeId());
        } else {
            for (VariableMapping mapping : messageNode.getVariableMappings()) {
                if (mapping.isPropertySelector()) {
                    selectors.add(
                            mapping.getName() + MESSAGE_SELECTOR_VALUE_DELIMITER + getMessageSelectorValue(variableProvider, messageNode, mapping));
                }
            }
        }
        Collections.sort(selectors);
        return Joiner.on(MESSAGE_SELECTOR_DELIMITER).join(selectors);
    }

    public static String getObjectMessageStrictSelector(Map<String, String> routingData) {
        return Joiner.on(MESSAGE_SELECTOR_DELIMITER).join(getObjectMessageSelectorSelectors(routingData));
    }

    public static Set<String> getObjectMessageCombinationSelectors(Map<String, String> routingData) {
        List<String> selectors = getObjectMessageSelectorSelectors(routingData);
        Set<String> result = Sets.newHashSet();
        for (Set<String> set : Sets.powerSet(Sets.newHashSet(selectors))) {
            if (set.isEmpty()) {
                continue;
            }
            List<String> list = Lists.newArrayList(set);
            Collections.sort(list);
            result.add(Joiner.on(MESSAGE_SELECTOR_DELIMITER).join(list));
        }
        return result;
    }

    private static List<String> getObjectMessageSelectorSelectors(Map<String, String> routingData) {
        List<String> selectors = Lists.newArrayList();
        for (Map.Entry<String, String> entry : routingData.entrySet()) {
            selectors.add(entry.getKey() + MESSAGE_SELECTOR_VALUE_DELIMITER + entry.getValue());
        }
        Collections.sort(selectors);
        return selectors;
    }

    public static void sendNodeAsyncExecutionMessage(Token token, boolean retry) {
        Connection connection = null;
        Session session = null;
        MessageProducer sender = null;
        try {
            init();
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            sender = session.createProducer(nodeAsyncExecutionQueue);
            ObjectMessage message = session.createObjectMessage();
            message.setLongProperty("processId", token.getProcess().getId());
            message.setLongProperty("tokenId", token.getId());
            message.setStringProperty("nodeId", token.getNodeId());
            message.setBooleanProperty("retry", retry);
            log.debug("sending node async execution request for " + token + ", retry=" + retry + "}");
            sender.send(message);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            releaseJmsSession(connection, session, sender);
        }
    }

    public static ObjectMessage sendEmailRequest(EmailConfig config) {
        Connection connection = null;
        Session session = null;
        MessageProducer sender = null;
        try {
            init();
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            sender = session.createProducer(emailQueue);
            ObjectMessage message = session.createObjectMessage(config);
            sender.send(message);
            sender.close();
            log.info("email request sent: " + message);
            return message;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            releaseJmsSession(connection, session, sender);
        }
    }

    @SuppressWarnings("unchecked")
    public static String toString(ObjectMessage message, boolean html) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append(message.getJMSMessageID());
            buffer.append(html ? "<br>" : "\n");
            if (message.getJMSExpiration() != 0) {
                buffer.append("{JMSExpiration=").append(CalendarUtil.formatDateTime(new Date(message.getJMSExpiration()))).append("}");
                buffer.append(html ? "<br>" : "\n");
            }
            Enumeration<String> propertyNames = message.getPropertyNames();
            Map<String, String> properties = new HashMap<String, String>();
            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement();
                String propertyValue = message.getStringProperty(propertyName);
                properties.put(propertyName, propertyValue);
            }
            buffer.append(properties);
            buffer.append(html ? "<br>" : "\n");
            if (message.getObject() instanceof Map) {
                buffer.append(TypeConversionUtil.toStringMap((Map<? extends Object, ? extends Object>) message.getObject()));
            } else if (message.getObject() != null) {
                buffer.append(message.getObject());
            }
            return buffer.toString();
        } catch (JMSException e) {
            throw Throwables.propagate(e);
        }
    }

    public static void rollbackTransaction(UserTransaction transaction) {
        int status = -1;
        try {
            if (transaction != null) {
                status = transaction.getStatus();
                if (status != Status.STATUS_NO_TRANSACTION) {
                    transaction.rollback();
                } else {
                    LogFactory.getLog(Utils.class).warn("Unable to rollback, status: " + status);
                }
            }
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to rollback, status: " + status, e);
        }
    }

    public static List<String> splitString(String string, String delimiter) {
        if (string != null) {
            return Splitter.on(delimiter).omitEmptyStrings().trimResults().splitToList(string);
        }
        return Lists.newArrayList();
    }

    public static boolean isNullOrEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            String s = (String) value;
            return s.trim().length() == 0;
        }
        if (value instanceof List) {
            List s = (List) value;
            return s.size() == 0;
        }
        if (value instanceof Map) {
            Map s = (Map) value;
            return s.isEmpty();
        }
        if (value instanceof Set) {
            Set s = (Set) value;
            return s.isEmpty();
        }
        return false;
    }

    /**
     * Check strings equality with trimming (Oracle specifics: null == '' == ' ').
     */
    public static boolean stringsEqual(String string1, String string2) {
        if (string1 == null) {
            return string2 == null || string2.trim().length() == 0;
        }
        if (string2 == null) {
            return string1.trim().length() == 0;
        }
        return string1.trim().equals(string2.trim());
    }

    public static boolean failProcessExecution(UserTransaction transaction, final Long tokenId, final Throwable throwable) {
        final AtomicBoolean needReprocessing = new AtomicBoolean(false);
        new TransactionalExecutor(transaction) {

            @Override
            protected void doExecuteInTransaction() throws Exception {
                Token token = ApplicationContextFactory.getTokenDAO().getNotNull(tokenId);
                if (token.hasEnded()) {
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

    public static String getCuttedString(String string, int limit) {
        if (string != null && string.length() > limit) {
            return string.substring(0, limit);
        }
        return string;
    }

    public static String getErrorMessage(Throwable throwable) {
        return getCuttedString(throwable.toString(), 1024 / 2);
    }

    public static Object getContainerCopy(Object object) {
        if (object instanceof List) {
            List copy = Lists.newArrayList();
            for (Object item : (List) object) {
                copy.add(getContainerCopy(item));
            }
            return copy;
        } else if (object instanceof UserTypeMap) {
            UserTypeMap userTypeMap = (UserTypeMap) object;
            UserTypeMap copy = new UserTypeMap(userTypeMap.getUserType());
            for (Map.Entry<String, Object> entry : userTypeMap.entrySet()) {
                copy.put(entry.getKey(), getContainerCopy(entry.getValue()));
            }
            return copy;
        } else if (object instanceof Map) {
            Map copy = Maps.newHashMap();
            for (Map.Entry entry : ((Map<Object, Object>) object).entrySet()) {
                copy.put(entry.getKey(), getContainerCopy(entry.getValue()));
            }
            return copy;
        }
        return object;
    }

}
