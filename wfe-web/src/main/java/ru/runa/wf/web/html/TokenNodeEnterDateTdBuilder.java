package ru.runa.wf.web.html;

import java.util.Date;
import ru.runa.common.web.html.BaseDateTdBuilder;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.commons.error.dto.WfTokenError;

public class TokenNodeEnterDateTdBuilder extends BaseDateTdBuilder<WfTokenError> {

    @Override
    protected Date getDate(WfTokenError error) {
        return error.getNodeEnterDate();
    }

    @Override
    protected Long getId(WfTokenError error) {
        return error.getProcessId();
    }

    @Override
    protected String getActionMapping() {
        return ShowGraphModeHelper.getManageProcessAction();
    }
}
