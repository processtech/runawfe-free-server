package ru.runa.common.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class RequestWebHelper implements WebHelper {
    private final HttpServletRequest request;

    public RequestWebHelper(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public String getMessage(String key) {
        return key;
    }

    @Override
    public String getUrl(String relativeUrl) {
        return "/" + relativeUrl;
    }

    @Override
    public String getActionUrl(String relativeUrl, Map<String, ? extends Object> params) {
        return null;
    }

    @Override
    public boolean useLinkForExecutor(User user, Executor executor) {
        return false;
    }

    @Override
    public WfProcess getProcess(User user, Long processId) {
        return null;
    }
}
