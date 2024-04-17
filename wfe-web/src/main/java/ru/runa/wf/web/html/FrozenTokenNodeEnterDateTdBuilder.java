package ru.runa.wf.web.html;

import java.util.Date;
import ru.runa.common.web.html.BaseDateTdBuilder;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.execution.dto.WfFrozenToken;

public class FrozenTokenNodeEnterDateTdBuilder extends BaseDateTdBuilder<WfFrozenToken> {

    @Override
    protected Date getDate(WfFrozenToken token) {
        return token.getNodeEnterDate();
    }

    @Override
    protected Long getId(WfFrozenToken token) {
        return token.getProcessId();
    }

    @Override
    protected String getActionMapping() {
        return ShowGraphModeHelper.getManageProcessAction();
    }
}
