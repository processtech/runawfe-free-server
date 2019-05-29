package ru.runa.wfe.commons.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public interface WebHelper {
    public static final String ACTION_VIEW_EXECUTOR = "/manage_executor";
    public static final String ACTION_VIEW_PROCESS = "/manage_process";
    public static final String ACTION_DOWNLOAD_LOG_FILE = "log_file";
    public static final String ACTION_DOWNLOAD_PROCESS_FILE = "process_file";
    public static final String ACTION_DOWNLOAD_SESSION_FILE = "/getSessionFile";
    public static final String PARAM_ID = "id";
    public static final String PARAM_FILE_NAME = "fileName";
    public static final String PARAM_VARIABLE_NAME = "variableName";

    String getMessage(String key);

    HttpServletRequest getRequest();

    String getUrl(String relativeUrl);

    String getActionUrl(String relativeUrl, Map<String, ? extends Object> params);

    boolean useLinkForExecutor(User user, Executor executor);

    /**
     * @return process or null in case of exception
     */
    WfProcess getProcess(User user, Long processId);
}
