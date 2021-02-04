package ru.runa.wf.web.servlet;

import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class GetUsersNamesForChatAjax extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        JSONObject outputObject = new JSONObject();
        Long processId = Long.parseLong(request.getParameter("processId"));
        JSONArray names = new JSONArray();
        JSONArray fullNames = new JSONArray();
        for (Executor executor : Delegates.getExecutionService().getAllExecutorsByProcessId(processId)) {
            if (executor instanceof Actor) {
                names.add(executor.getName());
                fullNames.add(executor.getFullName());
            }
        }
        outputObject.put("names", names);
        outputObject.put("fullNames", fullNames);
        return outputObject;
    }

}