package ru.runa.wf.web.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class GetHierarhyLevelAjax  extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        String chatId=request.getParameter("chatId");
        String messageId=request.getParameter("messageId");
        JSONObject object = new JSONObject();
        ChatMessage coreMessage=Delegates.getExecutionService().getChatMessage(Integer.parseInt(chatId), Long.parseLong(messageId));
        List<Integer> coreMessageHierarhy = coreMessage.getIerarchyMessageArray();
        if(coreMessageHierarhy.size()>0) {
            List<ChatMessage> messages = new ArrayList<ChatMessage>();
            for(int i=0;i<coreMessageHierarhy.size();i++){
                ChatMessage message0 = Delegates.getExecutionService().getChatMessage(Integer.parseInt(chatId), coreMessageHierarhy.get(i));
                if(message0 != null) {
                    messages.add(message0);
                }
                else{
                    message0 = new ChatMessage();
                    message0.setText("message deleted");
                    message0.setUserName("deleted");
                    message0.setId(-1);
                    message0.setIerarchyMessage("");
                    messages.add(message0);
                }
            }
            JSONArray messagesArray = new JSONArray();
            for (ChatMessage chatMessage : messages) {
                JSONObject object1 = new JSONObject();
                object1.put("id", chatMessage.getId());
                object1.put("text", chatMessage.getText());
                object1.put("author", chatMessage.getUserName());
                if(chatMessage.getIerarchyMessageArray().size()>0) {
                    object1.put("hierarchyMessageFlag", 1);
                }
                else {
                    object1.put("hierarchyMessageFlag", 0);
                }
                messagesArray.add(object1);
            }
            object.put("newMessage", 0);
            object.put("messages",messagesArray);
        }else {
            object.put("newMessage", 1);
        }
        return object;
    }
    
}
