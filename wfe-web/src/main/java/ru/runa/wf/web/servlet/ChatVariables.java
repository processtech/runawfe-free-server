package ru.runa.wf.web.servlet;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

public class ChatVariables extends JsonAjaxCommand {
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Long processId = Long.parseLong(request.getParameter("processId"));
        List<WfVariable> variable = Delegates.getExecutionService().getVariables(user, processId);
        JSONArray massiveVariable = new JSONArray();
        for (WfVariable v : variable) {
            massiveVariable.add(v);
        }
        JSONObject outputObject = new JSONObject();
        outputObject.put("chatVariables", massiveVariable);
        return outputObject;
    }

}
