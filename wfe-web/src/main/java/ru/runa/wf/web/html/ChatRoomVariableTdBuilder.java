package ru.runa.wf.web.html;

import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.var.dto.WfVariable;

public class ChatRoomVariableTdBuilder extends ProcessVariableTdBuilder {
    public ChatRoomVariableTdBuilder(String variableName) {
        super(variableName);
    }

    @Override
    protected WfVariable getVariable(Object object) {
        return ((WfChatRoom) object).getVariable(getVariableName());
    }

    @Override
    protected Long getId(Object object) {
        return ((WfChatRoom) object).getId();
    }
}
