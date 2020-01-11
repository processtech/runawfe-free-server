package ru.runa.wf.web.servlet;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class GetUsersNamesForChatAjax extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        JSONObject outputObject = new JSONObject();
        Long processId = Long.parseLong(request.getParameter("processId"));
        List<Executor> actors = Delegates.getAuthorizationService().getExecutorsWithPermission(user,
                Delegates.getExecutionService().getProcess(user, processId), null, true);
        // JSONArray userNames = new JSONArray();
        // JSONArray groupNames = new JSONArray();
        // JSONArray roleNames = new JSONArray();
        JSONArray names = new JSONArray();
        for (int i = 0; i < actors.size(); i++) {
            Class<? extends Executor> executorClass = actors.get(i).getClass();
            if (executorClass == Actor.class) {
                // userNames.add(actors.get(i).getName());
                names.add(actors.get(i).getName());
            } else if (executorClass == Group.class) {
                // groupNames.add(actors.get(i).getName());
                names.add(actors.get(i).getName());
            }
        }
        // outputObject.put("userNames", userNames);
        // outputObject.put("groupNames", groupNames);
        // outputObject.put("roleNames", roleNames);
        outputObject.put("names", names);
        return outputObject;
    }

}
