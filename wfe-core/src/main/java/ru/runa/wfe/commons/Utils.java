package ru.runa.wfe.commons;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
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
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dto.Variables;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Utils {
    public static final String CATEGORY_DELIMITER = "/";
    private static Log log = LogFactory.getLog(Utils.class);
    private static volatile InitialContext initialContext;
    private static TransactionManager transactionManager;
    private static ConnectionFactory connectionFactory;
    private static Queue bpmMessageQueue;
    private static Queue emailQueue;
    private static Queue nodeAsyncExecutionQueue;

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

    // TODO It is an anti-pattern to create new connections, sessions, producers and consumers for each message you produce or consume
    public static ObjectMessage sendBpmnMessage(List<VariableMapping> data, IVariableProvider variableProvider, long ttl) {
        Connection connection = null;
        Session session = null;
        MessageProducer sender = null;
        try {
            init();
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            sender = session.createProducer(bpmMessageQueue);
            HashMap<String, Object> map = new HashMap<String, Object>();
            for (VariableMapping variableMapping : data) {
                if (!variableMapping.isPropertySelector()) {
                    map.put(variableMapping.getMappedName(), variableProvider.getValue(variableMapping.getName()));
                }
            }
            ObjectMessage message = session.createObjectMessage(map);
            for (VariableMapping variableMapping : data) {
                if (variableMapping.isPropertySelector()) {
                    Object value = ExpressionEvaluator.evaluateVariableNotNull(variableProvider, variableMapping.getMappedName());
                    String stringValue = TypeConversionUtil.convertTo(String.class, value);
                    message.setStringProperty(variableMapping.getName(), stringValue);
                }
            }
            sender.send(message, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, ttl);
            sender.close();
            log.info("message sent: " + toString(message, false));
            return message;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            releaseJmsSession(connection, session, sender);
        }
    }

    public static void sendBpmnErrorMessage(Long processId, Long tokenId, String nodeId, Throwable throwable) {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(BaseMessageNode.EVENT_TYPE, MessageEventType.error.name());
        variables.put(BaseMessageNode.ERROR_EVENT_MESSAGE, throwable.getMessage());
        variables.put(BaseMessageNode.ERROR_EVENT_PROCESS_ID, processId);
        variables.put(BaseMessageNode.ERROR_EVENT_TOKEN_ID, tokenId);
        variables.put(BaseMessageNode.ERROR_EVENT_NODE_ID, nodeId);
        MapVariableProvider variableProvider = new MapVariableProvider(variables);
        List<VariableMapping> variableMappings = Lists.newArrayList();
        variableMappings.add(new VariableMapping(BaseMessageNode.EVENT_TYPE, Variables.wrap(BaseMessageNode.EVENT_TYPE),
                VariableMapping.USAGE_SELECTOR));
        variableMappings.add(new VariableMapping(BaseMessageNode.ERROR_EVENT_PROCESS_ID, Variables.wrap(BaseMessageNode.ERROR_EVENT_PROCESS_ID),
                VariableMapping.USAGE_SELECTOR));
        variableMappings.add(new VariableMapping(BaseMessageNode.ERROR_EVENT_NODE_ID, Variables.wrap(BaseMessageNode.ERROR_EVENT_NODE_ID),
                VariableMapping.USAGE_SELECTOR));
        variableMappings.add(new VariableMapping(BaseMessageNode.ERROR_EVENT_TOKEN_ID, BaseMessageNode.ERROR_EVENT_TOKEN_ID,
                VariableMapping.USAGE_READ));
        variableMappings
                .add(new VariableMapping(BaseMessageNode.ERROR_EVENT_MESSAGE, BaseMessageNode.ERROR_EVENT_MESSAGE, VariableMapping.USAGE_READ));
        Utils.sendBpmnMessage(variableMappings, variableProvider, 60000);
    }

    public static String getMessageSelectorValue(IVariableProvider variableProvider, BaseMessageNode messageNode, VariableMapping mapping) {
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

    public static String getReceiveMessageNodeHash(IVariableProvider variableProvider, BaseMessageNode messageNode) {
        int hash = 0;
        if (messageNode.getEventType() == MessageEventType.error && messageNode.getParentElement() instanceof Node) {
            hash += Objects.hashCode(BaseMessageNode.EVENT_TYPE + MessageEventType.error.name());
            hash += Objects.hashCode(BaseMessageNode.ERROR_EVENT_PROCESS_ID + String.valueOf(variableProvider.getProcessId()));
            hash += Objects.hashCode(BaseMessageNode.ERROR_EVENT_NODE_ID + ((Node) messageNode.getParentElement()).getNodeId());
        } else {
            for (VariableMapping mapping : messageNode.getVariableMappings()) {
                if (mapping.isPropertySelector()) {
                    hash += Objects.hashCode(mapping.getName() + getMessageSelectorValue(variableProvider, messageNode, mapping));
                }
            }
        }
        return String.valueOf(hash);
    }

    public static String getObjectMessageStrictHash(ObjectMessage message) throws JMSException {
        return String.valueOf(sum(getObjectMessageSelectorHashes(message)));
    }

    public static Set<String> getObjectMessageCombinationHashes(ObjectMessage message) throws JMSException {
        Set<Integer> hashes = getObjectMessageSelectorHashes(message);
        Set<String> result = Sets.newHashSet();
        for (Set<Integer> sets : Sets.powerSet(hashes)) {
            result.add(String.valueOf(sum(sets)));
        }
        return result;
    }

    private static Set<Integer> getObjectMessageSelectorHashes(ObjectMessage message) throws JMSException {
        Set<Integer> hashes = Sets.newHashSet();
        Enumeration<String> propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            if (!propertyName.startsWith("JMS")) {
                hashes.add(Objects.hashCode(propertyName + message.getStringProperty(propertyName)));
            }
        }
        return hashes;
    }

    private static int sum(Collection<Integer> collection) {
        // int sum = list.stream().mapToInt(Integer::intValue).sum();
        int sum = 0;
        for (int i : collection) {
            sum = sum + i;
        }
        return sum;
    }

    public static void sendNodeAsyncExecutionMessage(Long processId, Long tokenId, String nodeId) {
        Connection connection = null;
        Session session = null;
        MessageProducer sender = null;
        try {
            init();
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            sender = session.createProducer(nodeAsyncExecutionQueue);
            ObjectMessage message = session.createObjectMessage();
            message.setLongProperty("processId", processId);
            message.setLongProperty("tokenId", tokenId);
            message.setStringProperty("nodeId", nodeId);
            log.debug("sending node async execution request: {processId=" + processId + ", tokenId=" + tokenId + ", nodeId=" + nodeId + "}");
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
                if (status != Status.STATUS_NO_TRANSACTION && status != Status.STATUS_ROLLEDBACK) {
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

    public static void failProcessExecution(UserTransaction transaction, final Long tokenId, final Throwable throwable) {
        new TransactionalExecutor(transaction) {

            @Override
            protected void doExecuteInTransaction() throws Exception {
                Token token = ApplicationContextFactory.getTokenDAO().getNotNull(tokenId);
                if (token.getExecutionStatus() != ExecutionStatus.FAILED) {
                    token.fail(throwable);
                    token.getProcess().setExecutionStatus(ExecutionStatus.FAILED);
                    ProcessError processError = new ProcessError(ProcessErrorType.execution, token.getProcess().getId(), token.getNodeId());
                    processError.setThrowable(throwable);
                    Errors.sendEmailNotification(processError);
                }
            }
        }.executeInTransaction(true);
    }

    public static String getCuttedString(String string, int limit) {
        if (string != null && string.length() > limit) {
            return string.substring(0, limit);
        }
        return string;
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
