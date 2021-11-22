package ru.runa.wfe.commons.email;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.Map;
import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.VariableProvider;

@CommonsLog
public class EmailErrorNotifier {
    private static byte[] emailNotificationConfigBytes;

    static {
        if (SystemProperties.isErrorEmailNotificationEnabled()) {
            try {
                InputStream in = ClassLoaderUtil.getAsStream(SystemProperties.getErrorEmailNotificationConfiguration(), EmailErrorNotifier.class);
                if (in != null) {
                    emailNotificationConfigBytes = ByteStreams.toByteArray(in);
                    EmailConfigParser.parse(emailNotificationConfigBytes);
                } else {
                    log.error("Email notification configuration file not found: " + SystemProperties.getErrorEmailNotificationConfiguration());
                }
            } catch (Exception e) {
                log.error("Email notification configuration error", e);
                emailNotificationConfigBytes = null;
            }
        }
    }

    public static void sendNotification(final SystemError error) {
        if (emailNotificationConfigBytes != null) {
            if (error.getStackTrace() != null) {
                for (String filterExcludes : SystemProperties.getErrorEmailNotificationFilterExcludes()) {
                    if (error.getStackTrace().contains(filterExcludes)) {
                        log.debug("Ignored (filtered) email notification about " + error.getMessage());
                        return;
                    }
                }
            }
            // non-blocking usage for surrounding transaction
            new Thread() {
                @Override
                public void run() {
                    try {
                        EmailConfig config = EmailConfigParser.parse(emailNotificationConfigBytes);
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("error", error);
                        VariableProvider variableProvider = new MapVariableProvider(map);
                        config.applySubstitutions(variableProvider);
                        String formMessage = ExpressionEvaluator.process(null, config.getMessage(), variableProvider, null);
                        config.setMessage(formMessage);
                        config.setMessageId("Error: " + error.getMessage());
                        EmailUtils.sendMessage(config);
                    } catch (Exception e) {
                        log.error("Unable to send email notification about error", e);
                    }
                }
            }.start();
        }
    }

    public static void sendNotification(Long processId, String nodeId, String errorMessage, String stackTrace) {
        sendNotification(new ProcessError(processId, nodeId, errorMessage, stackTrace));
    }

    @Data
    private static class ProcessError extends SystemError {
        private Long processId;
        private String nodeId;

        public ProcessError(Long processId, String nodeId, String errorMessage, String stackTrace) {
            super(errorMessage, stackTrace);
            this.processId = processId;
            this.nodeId = nodeId;
        }
    }
}
