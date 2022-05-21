package ru.runa.wf.web.html;

import ru.runa.wfe.chat.dto.WfChatRoom;

public class ChatRoomErrorsTdBuilder extends ProcessErrorsTdBuilder {
    @Override
    public String getValue(Object object, Env env) {
        return ((WfChatRoom)object).getProcess().getErrors();
    }
}
