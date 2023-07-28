package ru.runa.wf.web.servlet;

import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

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
        List<WfSwimlane> swimlanes = Delegates.getExecutionService().getProcessSwimlanes(user, processId);
        WfSwimlane targetSwimlane = null;
        for (WfSwimlane swimlane : swimlanes) {
            if (swimlaneName.equals(swimlane.getDefinition().getName())) {
                targetSwimlane = swimlane;
                break;
            }
        }
        if (targetSwimlane == null) {
            throw new InternalApplicationException("swimlane not found");
        }
        Executor currentExecutor = targetSwimlane.getExecutor();
        JSONObject root = new JSONObject();
        if (currentExecutor == null) {
            root.put("currentExecutorName", "");
        } else if (currentExecutor instanceof Actor) {
            root.put("currentExecutorName", currentExecutor.getFullName());
        } else if (currentExecutor instanceof Group) {
            root.put("currentExecutorName", currentExecutor.getName());
        }
        return root;
    }
}
