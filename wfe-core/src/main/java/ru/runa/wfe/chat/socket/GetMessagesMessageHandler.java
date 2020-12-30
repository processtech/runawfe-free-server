package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.List;
import javax.websocket.Session;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.ChatGetMessagesDto;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.dto.ChatNewMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@Component
public class GetMessagesMessageHandler implements ChatSocketMessageHandler {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, String objectMessage, User user) throws IOException {
        ChatGetMessagesDto getMessagesDto = (ChatGetMessagesDto) ChatNewMessageDto.load(objectMessage, ChatGetMessagesDto.class);
        List<ChatMessageDto> messages;
        if (getMessagesDto.getLastMessageId() < 0) {
            getMessagesDto.setLastMessageId(chatLogic.getLastReadMessage(user.getActor(), getMessagesDto.getProcessId()));
        }
        messages = chatLogic.getMessages(user.getActor(), getMessagesDto.getProcessId(), getMessagesDto.getLastMessageId(),
                getMessagesDto.getCount());
        for (ChatMessageDto newMessage : messages) {
            newMessage.setOld(true);
            if (newMessage.getMessage().getCreateActor().equals(user.getActor())) {
                newMessage.setCoreUserFlag(true);
            }
            sessionHandler.sendToSession(session, newMessage.convert());
        }
        JSONObject sendUnblockOldMes = new JSONObject();
        sendUnblockOldMes.put("messType", "unblockOldMes");
        sessionHandler.sendToSession(session, sendUnblockOldMes.toString());
    }

    @Override
    public boolean checkType(String messageType) {
        return messageType.equals("getMessages");
    }

}
