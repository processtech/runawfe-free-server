package ru.runa.wf.web.servlet;

import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.common.web.AjaxWebHelper;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wf.web.tag.ProcessVariableMonitorTag;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

public class GetProcessVariableValueAjaxCommand extends JsonAjaxCommand {
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Long processId = Long.parseLong(request.getParameter("processId"));
        int index = Integer.parseInt(request.getParameter("index"));
        String date = request.getParameter("date");
        String variableName = request.getParameter("variableName");
        WfVariable variable = date.equals("null")
                ? Delegates.getExecutionService().getVariable(user, processId, variableName)
                : ProcessVariableMonitorTag.getHistoricalVariables(user, date, processId, variableName).stream()
                .findAny().orElseThrow(() -> new IllegalArgumentException(variableName));
        JSONObject object = new JSONObject();
        object.put("text", ViewUtil.getOutput(user, new AjaxWebHelper(request), processId, variable));
        object.put("index", Integer.toString(index));
        return object;
    }
}
