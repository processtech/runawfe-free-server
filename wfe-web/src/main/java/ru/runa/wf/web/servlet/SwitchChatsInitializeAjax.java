package ru.runa.wf.web.servlet;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class SwitchChatsInitializeAjax extends JsonAjaxCommand {
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        List<Long> chatIds = Delegates.getChatService().getActiveChatIds(user);
        List<Long> countMessages = Delegates.getChatService().getNewMessagesCounts(user, chatIds);
        JSONArray outputObjects = new JSONArray();
        JSONObject outObj;
        for (int i = 0; i < countMessages.size(); i++) {
            outObj = new JSONObject();
            outObj.put("processId", chatIds.get(i));
            outObj.put("countMessage", countMessages.get(i));
            outputObjects.add(outObj);
        }

        return outputObjects;
    }
}
