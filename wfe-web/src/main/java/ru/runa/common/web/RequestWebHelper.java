package ru.runa.common.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class RequestWebHelper implements WebHelper {
    protected final HttpServletRequest request;

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
        return Delegates.getAuthorizationService().isAllowed(user, Permission.LIST, executor);
    }

    @Override
    public WfProcess getProcess(User user, Long processId) {
        return Delegates.getExecutionService().getProcess(user, processId);
    }
}
