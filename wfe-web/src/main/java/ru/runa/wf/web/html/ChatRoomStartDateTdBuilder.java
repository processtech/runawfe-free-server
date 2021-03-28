package ru.runa.wf.web.html;

import ru.runa.common.web.html.BaseDateTdBuilder;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.chat.dto.WfChatRoom;
import java.util.Date;

public class ChatRoomStartDateTdBuilder extends BaseDateTdBuilder<WfChatRoom> {

    @Override
    protected Date getDate(WfChatRoom object) {
        return object.getStartDate();
    }

    @Override
    protected Long getId(WfChatRoom object) {
        return object.getId();
    }

    @Override
    protected String getActionMapping() {
        return ShowGraphModeHelper.getManageProcessAction();
    }
}
