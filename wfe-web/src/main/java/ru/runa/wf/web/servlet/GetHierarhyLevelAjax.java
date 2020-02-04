package ru.runa.wf.web.servlet;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class GetHierarhyLevelAjax extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        String messageId = request.getParameter("messageId");
        JSONObject outputObject = new JSONObject();
        ChatMessage chatMessage = Delegates.getChatService().getChatMessage(user, Long.parseLong(messageId));
        ArrayList<Long> quotedMessageIds = new ArrayList<Long>();
        String[] ids = chatMessage.getQuotedMessageIds().split(":");
        for (int i = 0; i < ids.length; i++) {
            if (!(ids[i].isEmpty())) {
                quotedMessageIds.add(Long.parseLong(ids[i]));
            }
        }
        if (quotedMessageIds.size() > 0) {
            JSONArray messagesArrayObject = new JSONArray();
            for (int i = 0; i < quotedMessageIds.size(); i++) {
                ChatMessage attachedMessage = Delegates.getChatService().getChatMessage(user, quotedMessageIds.get(i));
                JSONObject attachedMesObject = new JSONObject();
                if (attachedMessage != null) {
                    attachedMesObject.put("id", attachedMessage.getId());
                    attachedMesObject.put("text", attachedMessage.getText());
                    attachedMesObject.put("author", attachedMessage.getCreateActor().getName());
                    attachedMesObject.put("hierarchyMessageFlag", StringUtils.isNotBlank(attachedMessage.getQuotedMessageIds()));
                } else {
                    attachedMesObject.put("id", -1);
                    attachedMesObject.put("text", "message deleted");
                    attachedMesObject.put("author", "deleted");
                    attachedMesObject.put("hierarchyMessageFlag", 0);
                }
                messagesArrayObject.add(attachedMesObject);
            }
            outputObject.put("newMessage", 0);
            outputObject.put("messages", messagesArrayObject);
        } else {
            outputObject.put("newMessage", 1);
        }
        return outputObject;
    }

}
