package ru.runa.wfe.commons;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.VariableProvider;

public class Errors {
    private static final Log LOG = LogFactory.getLog(Errors.class);
    private static Set<SystemError> systemErrors = Collections.synchronizedSet(new HashSet<>());
    private static ConcurrentHashMap<Long, Set<ProcessError>> processErrors = new ConcurrentHashMap<>();
    private static byte[] emailNotificationConfigBytes;

    static {
        if (SystemProperties.isErrorEmailNotificationEnabled()) {
            try {
                InputStream in = ClassLoaderUtil.getAsStream(SystemProperties.getErrorEmailNotificationConfiguration(), Errors.class);
                if (in != null) {
                    emailNotificationConfigBytes = ByteStreams.toByteArray(in);
                    EmailConfigParser.parse(emailNotificationConfigBytes);
                } else {
                    LOG.error("Email notification configuration file not found: " + SystemProperties.getErrorEmailNotificationConfiguration());
                }
            } catch (Exception e) {
                LOG.error("Email notification configuration error", e);
                emailNotificationConfigBytes = null;
            }
        }
    }

    public static List<SystemError> getSystemErrors() {
        synchronized (systemErrors) {
            List<SystemError> list = new ArrayList<>(systemErrors);
            Collections.sort(list);
            return list;
        }
    }

    public static List<ProcessError> getAllProcessErrors() {
        List<ProcessError> result = new ArrayList<>();
        for (Set<ProcessError> errors : processErrors.values()) {
            synchronized (errors) {
                result.addAll(errors);
            }
        }
        return result;
    }

    public static List<ProcessError> getProcessErrors(Long processId) {
        Set<ProcessError> errors = processErrors.get(processId);
        if (errors != null) {
            synchronized (errors) {
                return new ArrayList<>(errors);
            }
        }
        return new ArrayList<>();
    }

    public static void addSystemError(Throwable throwable) {
        SystemError systemError = new SystemError(throwable);
        boolean alreadyExists = systemErrors.add(systemError);
        if (!alreadyExists) {
            sendEmailNotification(systemError);
        }
    }

    public static void removeSystemError(String errorMessage) {
        synchronized (systemErrors) {
            for (SystemError systemError : systemErrors) {
                if (Objects.equal(systemError.getMessage(), errorMessage)) {
                    systemErrors.remove(systemError);
                    break;
                }
            }
        }
    }

    public static boolean addProcessError(ProcessError processError, String nodeName, Throwable throwable) {
        processError.setNodeName(nodeName);
        processError.setThrowable(throwable);
        Set<ProcessError> errors = processErrors.computeIfAbsent(processError.getProcessId(), new Function<Long, Set<ProcessError>>() {
            @Override
            public Set<ProcessError> apply(Long t) {
                return Collections.synchronizedSet(new HashSet<>());
            }
        });
        boolean added = errors.add(processError);
        if (added) {
            sendEmailNotification(processError);
        }
        return added;
    }

    public static void removeProcessError(ProcessError processError) {
        Set<ProcessError> list = processErrors.get(processError.getProcessId());
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

    public static void sendEmailNotification(final SystemError error) {
        if (emailNotificationConfigBytes != null) {
            if (error.getStackTrace() != null) {
                for (String filterExcludes : SystemProperties.getErrorEmailNotificationFilterExcludes()) {
                    if (error.getStackTrace().contains(filterExcludes)) {
                        LOG.debug("Ignored (filtered) email notification about " + error.getMessage());
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
                        // does not work EmailUtils.sendMessageRequest(config);
                        EmailUtils.sendMessage(config);
                    } catch (Exception e) {
                        LOG.error("Unable to send email notification about error", e);
                    }
                };
            }.start();
        }
    }

}
