package ru.runa.wfe.commons.email;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.var.VariableProvider;

public class EmailConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(EmailConfig.class);
    public static final String COMMON_THROW_ERROR_ON_FAILURE = "throwErrorOnFailure";
    public static final String COMMON_BASE_PROPERTY_FILE_NAME = "basePropertiesFileName";
    public static final String CONNECTION_MAIL_TRANSPORT = "mail.transport.protocol";
    public static final String CONNECTION_MAIL_HOST = "mail.host";
    public static final String CONNECTION_MAIL_USER = "mail.user";
    public static final String CONNECTION_MAIL_PASSWORD = "mail.password";
    public static final String HEADER_TO = "To";
    public static final String HEADER_CC = "Cc";
    public static final String CONTENT_USE_MESSAGE_FROM_TASK_FORM = "bodyInlined";
    public static final String CONTENT_MESSAGE_TYPE = "bodyType";

    private final Map<String, String> commonProperties = Maps.newHashMap();
    private final Map<String, String> connectionProperties = Maps.newHashMap();
    private final Map<String, String> headerProperties = Maps.newHashMap();
    private final Map<String, String> contentProperties = Maps.newHashMap();
    private final List<String> attachmentVariableNames = Lists.newArrayList();
    private final List<Attachment> attachments = Lists.newArrayList();
    private String messageId;
    private String message = "";

    public Map<String, String> getCommonProperties() {
        return commonProperties;
    }

    public Map<String, String> getConnectionProperties() {
        return connectionProperties;
    }

    public Map<String, String> getHeaderProperties() {
        return headerProperties;
    }

    public Map<String, String> getContentProperties() {
        return contentProperties;
    }

    public List<String> getAttachmentVariableNames() {
        return attachmentVariableNames;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (!Strings.isNullOrEmpty(message)) {
            // important: base configuration value can be overridden by empty
            this.message = message;
        }
    }

    public boolean isThrowErrorOnFailure() {
        if (getCommonProperties().containsKey(COMMON_THROW_ERROR_ON_FAILURE)) {
            return Boolean.valueOf(getCommonProperties().get(COMMON_THROW_ERROR_ON_FAILURE));
        }
        return Boolean.TRUE;
    }

    public boolean isUseMessageFromTaskForm() {
        if (getContentProperties().containsKey(CONTENT_USE_MESSAGE_FROM_TASK_FORM)) {
            return Boolean.valueOf(getContentProperties().get(CONTENT_USE_MESSAGE_FROM_TASK_FORM));
        }
        return Boolean.FALSE;
    }

    public String getMessageType() {
        if (getContentProperties().containsKey(CONTENT_MESSAGE_TYPE)) {
            return getContentProperties().get(CONTENT_MESSAGE_TYPE);
        }
        return "html";
    }

    public void checkValid() {
        Preconditions.checkNotNull(message, "Message is null");
        if (connectionProperties.size() == 0) {
            throw new RuntimeException("Invalid configuration: connectionProperties.size() == 0");
        }
        if (headerProperties.size() == 0) {
            throw new RuntimeException("Invalid configuration: headerProperties.size() == 0");
        }
    }

    public void applySubstitutions(VariableProvider variableProvider) {
        applySubstitutions(variableProvider, connectionProperties);
        applySubstitutions(variableProvider, headerProperties);
    }

    private void applySubstitutions(VariableProvider variableProvider, Map<String, String> map) {
        for (Map.Entry<String, String> entry : new HashMap<String, String>(map).entrySet()) {
            String substitutedValue = ExpressionEvaluator.process(null, entry.getValue(), variableProvider, null);
            if (!Objects.equal(substitutedValue, entry.getValue())) {
                log.debug("Substituted " + entry + " -> " + substitutedValue);
            }
            map.put(entry.getKey(), substitutedValue);
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        Map<String, String> debugConnectionProperties = Maps.newHashMap(connectionProperties);
        if (debugConnectionProperties.containsKey(CONNECTION_MAIL_PASSWORD)) {
            debugConnectionProperties.put(CONNECTION_MAIL_PASSWORD, "*******");
        }
        buffer.append("\nConnection settings: ").append(debugConnectionProperties);
        buffer.append("\nHeaders: ").append(headerProperties);
        buffer.append("\nMessage: ").append(message);
        return buffer.toString();
    }

    public static class Attachment implements Serializable {
        private static final long serialVersionUID = 1L;
        public boolean inlined;
        public String fileName;
        public byte[] content;
    }

}
