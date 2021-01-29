package ru.runa.wf.web.servlet;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.chat.dto.broadcast.AddedMessageBroadcast;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ChatInitializeAjax extends JsonAjaxCommand {

    private final ObjectMapper chatObjectMapper = ApplicationContextFactory.getChatObjectMapper();

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Long processId = Long.parseLong(request.getParameter("processId"));
        int countMessages = Integer.parseInt(request.getParameter("messageCount"));
        JSONObject outputObject = new JSONObject();
        Long lastMessageId = Delegates.getChatService().getLastReadMessage(user, processId);
        List<AddedMessageBroadcast> messages;
        JSONArray messagesArrayObject = new JSONArray();
        outputObject.put("lastMessageId", lastMessageId);
        messages = Delegates.getChatService().getNewChatMessages(user, processId);
        AddedMessageBroadcast messageObject;
        if (messages.size() > 0) {
            messageObject = messages.get(0);
            messageObject.setOld(true);
            messageObject.setCoreUser(messageObject.getCreateActor().equals(user.getActor()));
            messagesArrayObject.add(chatObjectMapper.writeValueAsString(messages.get(0)));
            for (int i = 1; i < messages.size(); i++) {
                messageObject = messages.get(i);
                messageObject.setOld(false);
                messageObject.setCoreUser(messageObject.getCreateActor().equals(user.getActor()));
                messagesArrayObject.add(chatObjectMapper.writeValueAsString(messageObject));
            }
        }
        if (messages.size() < countMessages) {
            messages = Delegates.getChatService().getChatMessages(user, processId, lastMessageId, countMessages - messages.size());
            for (AddedMessageBroadcast message : messages) {
                messageObject = message;
                messageObject.setOld(true);
                messageObject.setCoreUser(messageObject.getCreateActor().equals(user.getActor()));
                messagesArrayObject.add(chatObjectMapper.writeValueAsString(messageObject));
            }
        }
        outputObject.put("messages", messagesArrayObject);
        return outputObject;
    }
}
