package ru.runa.wfe.var.format;

import java.util.HashMap;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

public class ProcessIdFormat extends LongFormat implements VariableDisplaySupport {

    @Override
    public String getName() {
        return "processref";
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

}
