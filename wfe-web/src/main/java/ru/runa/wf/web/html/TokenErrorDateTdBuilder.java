package ru.runa.wf.web.html;

import ru.runa.common.web.html.BaseDateTdBuilder;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import java.util.Date;

public class TokenErrorDateTdBuilder extends BaseDateTdBuilder<WfTokenError> {

    @Override
    protected Date getDate(WfTokenError error) {
        return error.getErrorDate();
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
