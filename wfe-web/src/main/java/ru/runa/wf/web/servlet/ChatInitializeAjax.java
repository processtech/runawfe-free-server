package ru.runa.wf.web.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.common.web.ChatSoket;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ChatInitializeAjax extends JsonAjaxCommand {
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Integer chatId = Integer.parseInt(request.getParameter("chatId"));
        Integer countMessages = Integer.parseInt(request.getParameter("messageCount"));
        JSONObject outputObject = new JSONObject();
        ChatsUserInfo chatUserInfo = Delegates.getExecutionService().getChatUserInfo(user.getActor(), chatId);
        List<ChatMessage> messages;
        JSONArray messagesArrayObject = new JSONArray();
        outputObject.put("lastMessageId", chatUserInfo.getLastMessageId());
        messages = Delegates.getExecutionService().getChatNewMessages(chatId, chatUserInfo.getLastMessageId());
        for (int i = 0; i < messages.size(); i++) {
            JSONObject messageObject = ChatSoket.convertMessage(messages.get(i), false);
            messagesArrayObject.add(messageObject);
        }
        if (messages.size() < countMessages) {// дополняем старыми
            messages = Delegates.getExecutionService().getChatMessages(chatId, chatUserInfo.getLastMessageId(), countMessages - messages.size());
            for (int i = 0; i < messages.size(); i++) {
                JSONObject messageObject = ChatSoket.convertMessage(messages.get(i), true);
                messagesArrayObject.add(messageObject);
            }
        }
        outputObject.put("messages", messagesArrayObject);
        return outputObject;
    }
}
