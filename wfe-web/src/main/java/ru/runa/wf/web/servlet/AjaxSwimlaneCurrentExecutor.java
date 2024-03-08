package ru.runa.wf.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

public class AjaxSwimlaneCurrentExecutor extends JsonAjaxCommand {

    @SuppressWarnings("unchecked")
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        JSONObject options;
        try {
            JSONParser jsonParser = new JSONParser();
            options = (JSONObject) jsonParser.parse(request.getReader());
        } catch (Exception e) {
            log.error("cannot parse JSON request", e);
            throw new ServletException(e);
        }
        Long processId = (Long) options.get("processId");
        String swimlaneName = (String) options.get("swimlaneName");
        if (processId == null) {
            throw new InternalApplicationException("processId cannot be empty");
        }
        if (swimlaneName == null || swimlaneName.isEmpty()) {
            throw new InternalApplicationException("swimlaneName cannot be empty");
        }
        WfVariable swimlane = Delegates.getExecutionService().getVariable(user, processId, swimlaneName);
        Executor currentExecutor = (Executor) swimlane.getValue();
        JSONObject root = new JSONObject();
        root.put("currentExecutorName", currentExecutor != null ? currentExecutor.getFullName() : "");
        return root;
    }

}
