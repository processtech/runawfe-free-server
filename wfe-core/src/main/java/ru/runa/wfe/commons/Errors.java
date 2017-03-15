package ru.runa.wfe.commons;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapVariableProvider;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

public class Errors {
    private static Set<SystemError> systemErrors = Sets.newConcurrentHashSet();
    private static Map<Long, List<ProcessError>> processErrors = Maps.newConcurrentMap();

    public static Set<SystemError> getSystemErrors() {
        return systemErrors;
    }

    public static Map<Long, List<ProcessError>> getProcessErrors() {
        return processErrors;
    }

    public static List<ProcessError> getProcessErrors(Long processId) {
        return processErrors.get(processId);
    }

    public static void addSystemError(Throwable throwable) {
        SystemError systemError = new SystemError(throwable);
        boolean alreadyExists = systemErrors.add(systemError);
        if (!alreadyExists) {
            sendEmailNotification(systemError);
        }
    }

    public static void removeSystemError(String errorMessage) {
        for (SystemError systemError : systemErrors) {
            if (Objects.equal(systemError.getMessage(), errorMessage)) {
                systemErrors.remove(systemError);
                break;
            }
        }
    }

    public static boolean addProcessError(ProcessError processError, String nodeName, Throwable throwable) {
        processError.setNodeName(nodeName);
        processError.setThrowable(throwable);
        List<ProcessError> list = processErrors.get(processError.getProcessId());
        if (list == null) {
            list = Lists.newArrayList();
            processErrors.put(processError.getProcessId(), list);
        }
        boolean alreadyExists = list.remove(processError);
        list.add(processError);
        if (!alreadyExists) {
            sendEmailNotification(processError);
        }
        return !alreadyExists;
    }

    public static void removeProcessError(ProcessError processError) {
        List<ProcessError> list = processErrors.get(processError.getProcessId());
        if (list != null) {
            list.remove(processError);
            if (list.isEmpty()) {
                processErrors.remove(processError.getProcessId());
            }
        }
    }

    public static void removeProcessErrors(Long processId) {
        processErrors.remove(processId);
    }

    private static byte[] emailNotificationConfigBytes;
    static {
        if (SystemProperties.isErrorEmailNotificationEnabled()) {
            try {
                InputStream in = ClassLoaderUtil.getAsStreamNotNull(SystemProperties.getErrorEmailNotificationConfiguration(), Errors.class);
                emailNotificationConfigBytes = ByteStreams.toByteArray(in);
                EmailConfigParser.parse(emailNotificationConfigBytes);
            } catch (Exception e) {
                LogFactory.getLog(Errors.class).error("Email notification configuration error", e);
                emailNotificationConfigBytes = null;
            }
        }
    }

    public static void sendEmailNotification(final SystemError error) {
        // non-blocking usage for surrounding transaction
        new Thread() {
            @Override
            public void run() {
                try {
                    if (emailNotificationConfigBytes != null) {
                        EmailConfig config = EmailConfigParser.parse(emailNotificationConfigBytes);
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("error", error);
                        IVariableProvider variableProvider = new MapVariableProvider(map);
                        config.applySubstitutions(variableProvider);
                        String formMessage = ExpressionEvaluator.process(null, config.getMessage(), variableProvider, null);
                        config.setMessage(formMessage);
                        config.setMessageId("Error: " + error.getMessage());
                        // does not work EmailUtils.sendMessageRequest(config);
                        EmailUtils.sendMessage(config);
                    }
                } catch (Exception e) {
                    LogFactory.getLog(EmailUtils.class).error("Unable to send email notification about error", e);
                }
            };
        }.start();
    }

}
