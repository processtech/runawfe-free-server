package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.List;
import javax.websocket.Session;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.dto.ChatGetMessagesDto;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@Component
public class GetMessagesMessageHandler implements ChatSocketMessageHandler<ChatGetMessagesDto> {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, ChatGetMessagesDto dto, User user) throws IOException {
        List<ChatMessageDto> messages;
        if (dto.getLastMessageId() < 0) {
            dto.setLastMessageId(chatLogic.getLastReadMessage(user.getActor(), dto.getProcessId()));
        }
        messages = chatLogic.getMessages(user.getActor(), dto.getProcessId(), dto.getLastMessageId(),
                dto.getCount());
        for (ChatMessageDto newMessage : messages) {
            newMessage.setOld(true);
            if (newMessage.getMessage().getCreateActor().equals(user.getActor())) {
                newMessage.setCoreUser(true);
            }
            sessionHandler.sendToSession(session, newMessage.convert());
        }
        JSONObject sendUnblockOldMes = new JSONObject();
        sendUnblockOldMes.put("messageType", "unblockOldMes");
        sessionHandler.sendToSession(session, sendUnblockOldMes.toString());
    }

    @Override
    public boolean isSupports(Class<? extends ChatDto> messageType) {
        return messageType.equals(ChatGetMessagesDto.class);
    }
}
