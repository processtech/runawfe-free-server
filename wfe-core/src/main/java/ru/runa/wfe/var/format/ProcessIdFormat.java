package ru.runa.wfe.var.format;

import java.util.HashMap;

import com.google.common.collect.Maps;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.User;

public class ProcessIdFormat extends VariableFormat implements VariableDisplaySupport {

    @Override
    public Class<? extends Number> getJavaClass() {
        return Long.class;
    }

    @Override
    public String getName() {
        return "processref";
    }

    @Override
    protected Long convertFromStringValue(String source) {
        return Long.valueOf(source);
    }

    @Override
    protected String convertToStringValue(Object obj) {
        return obj.toString();
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long currentProcessId, String name, Object object) {
        Long processId = (Long) object;
        try {
            WfProcess process = webHelper.getProcess(user, processId);
            HashMap<String, Object> params = Maps.newHashMap();
            params.put(WebHelper.PARAM_ID, processId);
            String href = webHelper.getActionUrl(WebHelper.ACTION_VIEW_PROCESS, params);
            return "<a href=\"" + href + "\" title=\"" + process.getName() + "\" target=\"process" + processId + "\">" + processId + "</a>";
        } catch (AuthorizationException e) {
            return "<span>" + processId + "</span>";
        }
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onProcessId(this, context);
    }

    @Override
    public String formatHtmlForExcelExport(User user, WebHelper webHelper, Long processId, String name, Object object) {
        return formatHtml(user, webHelper, processId, name, object);
    }

}
