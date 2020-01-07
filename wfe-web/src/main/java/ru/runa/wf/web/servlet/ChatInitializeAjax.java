package ru.runa.wf.web.servlet;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.common.web.ChatSocket;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ChatInitializeAjax extends JsonAjaxCommand {
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Long processId = Long.parseLong(request.getParameter("processId"));
        Integer countMessages = Integer.parseInt(request.getParameter("messageCount"));
        JSONObject outputObject = new JSONObject();
        ChatsUserInfo chatUserInfo = Delegates.getChatService().getChatUserInfo(user.getActor(), processId);
        List<ChatMessage> messages;
        JSONArray messagesArrayObject = new JSONArray();
        outputObject.put("lastMessageId", chatUserInfo.getLastMessageId());
        messages = Delegates.getChatService().getNewChatMessages(processId, chatUserInfo.getLastMessageId());
        if (messages.size() > 0) {
            JSONObject messageObject = ChatSocket.convertMessage(messages.get(0), true);
            if (messages.get(0).getActor().equals(user.getActor())) {
                messageObject.put("coreUser", true);
            }
            messagesArrayObject.add(messageObject);

            for (int i = 1; i < messages.size(); i++) {
                messageObject = ChatSocket.convertMessage(messages.get(i), false);
                if (messages.get(i).getActor().equals(user.getActor())) {
                    messageObject.put("coreUser", true);
                }
                messagesArrayObject.add(messageObject);
            }
        }
        if (messages.size() < countMessages) {// дополняем старыми
            messages = Delegates.getChatService().getChatMessages(processId, chatUserInfo.getLastMessageId(), countMessages - messages.size());
            for (int i = 0; i < messages.size(); i++) {
                JSONObject messageObject = ChatSocket.convertMessage(messages.get(i), true);
                if (messages.get(i).getActor().equals(user.getActor())) {
                    messageObject.put("coreUser", true);
                }
                messagesArrayObject.add(messageObject);
            }
        }
        outputObject.put("messages", messagesArrayObject);
        return outputObject;
    }
}
