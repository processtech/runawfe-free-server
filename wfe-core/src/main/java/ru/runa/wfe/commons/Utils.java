package ru.runa.wfe.commons;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dto.Variables;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Utils {
    public static final String CATEGORY_DELIMITER = "/";
    private static Log log = LogFactory.getLog(Utils.class);
    private static InitialContext initialContext;
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

    // FIXME It is an anti-pattern to create new connections, sessions, producers and consumers for each message you produce or consume
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
            buffer.append(message.toString());
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

}
