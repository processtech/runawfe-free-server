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
            sendEmailNotification(throwable, null);
        }
    }

    public static void removeSystemError(String message) {
        systemErrors.remove(message);
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
            sendEmailNotification(throwable, processError);
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

    public static void sendEmailNotification(final Throwable exception, final ProcessError processError) {
        // non-blocking usage for surrounding transaction
        new Thread() {
            @Override
            public void run() {
                try {
                    if (emailNotificationConfigBytes != null) {
                        boolean matches = false;
                        EmailConfig config = EmailConfigParser.parse(emailNotificationConfigBytes);
                        List<String> includes = Utils.splitString(config.getCommonProperties().get("exception.includes"), ";");
                        for (String className : includes) {
                            if (ClassLoaderUtil.loadClass(className).isInstance(exception)) {
                                matches = true;
                                break;
                            }
                        }
                        if (matches) {
                            List<String> excludes = Utils.splitString(config.getCommonProperties().get("exception.excludes"), ";");
                            for (String className : excludes) {
                                if (ClassLoaderUtil.loadClass(className).isInstance(exception)) {
                                    matches = false;
                                    break;
                                }
                            }
                        }
                        if (!matches) {
                            return;
                        }
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("exceptionClassName", exception.getClass().getName());
                        map.put("exceptionMessage", exception.getLocalizedMessage());
                        map.put("processError", processError);
                        IVariableProvider variableProvider = new MapVariableProvider(map);
                        config.applySubstitutions(variableProvider);
                        String formMessage = ExpressionEvaluator.process(null, config.getMessage(), variableProvider, null);
                        config.setMessage(formMessage);
                        config.setMessageId("Error: " + exception.toString());
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
