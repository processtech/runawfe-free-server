package ru.runa.wf.web.servlet;

import com.google.common.base.Strings;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

public class AjaxGetProcessVariablesList extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        try {
            Long processId = Long.valueOf(request.getParameter("processId"));
            String hint = request.getParameter("hint");
            WfProcess process = Delegates.getExecutionService().getProcess(user, processId);
            List<VariableDefinition> variables = Delegates.getDefinitionService().getVariableDefinitions(user, process.getDefinitionId());
            JSONObject root = new JSONObject();
            JSONArray data = new JSONArray();
            hint = hint.toLowerCase();
            for (VariableDefinition variable : variables) {
                String name = variable.getName();
                if (Strings.isNullOrEmpty(hint) || name.toLowerCase().startsWith(hint)) {
                    data.add(variable.getName());
                }
            }
            root.put("data", data);
            return root;
        } catch (Exception e) {
            log.error("Bad request", e);
            throw new InternalApplicationException(e);
        }
    }
}
