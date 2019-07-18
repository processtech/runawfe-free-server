package ru.runa.wf.web.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
        String chatId = request.getParameter("chatId");
        String messageId = request.getParameter("messageId");
        JSONObject outputObject = new JSONObject();
        ChatMessage coreMessage = Delegates.getExecutionService().getChatMessage(Integer.parseInt(chatId), Long.parseLong(messageId));
        List<Integer> coreMessageHierarhy = coreMessage.getIerarchyMessageArray();
        if (coreMessageHierarhy.size() > 0) {
            JSONArray messagesArrayObject = new JSONArray();
            for (int i = 0; i < coreMessageHierarhy.size(); i++) {
                ChatMessage attachedMessage = Delegates.getExecutionService().getChatMessage(Integer.parseInt(chatId), coreMessageHierarhy.get(i));
                if (attachedMessage == null) {
                    attachedMessage = new ChatMessage();
                    attachedMessage.setText("message deleted");
                    attachedMessage.setUserName("deleted");
                    attachedMessage.setId(-1);
                    attachedMessage.setIerarchyMessage("");
                }
                JSONObject attachedMesObject = new JSONObject();
                attachedMesObject.put("id", attachedMessage.getId());
                attachedMesObject.put("text", attachedMessage.getText());
                attachedMesObject.put("author", attachedMessage.getUserName());
                if (attachedMessage.getIerarchyMessageArray().size() > 0) {
                    attachedMesObject.put("hierarchyMessageFlag", 1);
                } else {
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
