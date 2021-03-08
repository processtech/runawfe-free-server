package ru.runa.wfe.commons.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public interface WebHelper {
    String ACTION_VIEW_EXECUTOR = "/manage_executor";
    String ACTION_VIEW_PROCESS = "/manage_process";
    String ACTION_DOWNLOAD_LOG_FILE = "log_file";
    String ACTION_DOWNLOAD_PROCESS_FILE = "process_file";
    String ACTION_DOWNLOAD_SESSION_FILE = "/getSessionFile";
    String PARAM_ID = "id";
    String PARAM_FILE_NAME = "fileName";
    String PARAM_VARIABLE_NAME = "variableName";

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
