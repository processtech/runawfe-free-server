package ru.runa.wf.web.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

public class GetUsersNamesForChatAjax extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        JSONObject outputObject = new JSONObject();
        String chatId = request.getParameter("chatId");
        List<Actor> actors = Delegates.getExecutionService().getAllUsersNamesForChat(Integer.parseInt(chatId));
        JSONArray names = new JSONArray();
        for (int i = 0; i < actors.size(); i++) {
            names.add(actors.get(i).getName());
        }
        outputObject.put("names", names);
        return outputObject;
    }

}
