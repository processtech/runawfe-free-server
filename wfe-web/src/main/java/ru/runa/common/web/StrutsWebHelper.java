package ru.runa.common.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

public class StrutsWebHelper implements WebHelper {
    private final PageContext pageContext;

    public StrutsWebHelper(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public String getMessage(String key) {
        return Commons.getMessage(key, pageContext);
    }

    @Override
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) pageContext.getRequest();
    }

    @Override
    public String getUrl(String relativeUrl) {
        return Commons.getUrl(relativeUrl, pageContext, PortletUrlType.Resource);
    }

    @Override
    public String getActionUrl(String relativeUrl, Map<String, ?> params) {
        if (ACTION_DOWNLOAD_PROCESS_FILE.equals(relativeUrl)) {
            return Commons.getActionUrl("/variableDownloader", params, pageContext, PortletUrlType.Render);
        }
        if (ACTION_DOWNLOAD_LOG_FILE.equals(relativeUrl)) {
            Map<String, Object> adjusted = Maps.newHashMap();
            adjusted.put("logId", params.remove(PARAM_ID));
            return Commons.getActionUrl("/variableDownloader", adjusted, pageContext, PortletUrlType.Render);
        }
        return Commons.getActionUrl(relativeUrl, params, pageContext, PortletUrlType.Render);
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
