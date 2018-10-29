package ru.runa.wf.web.html;

import java.util.Date;

import ru.runa.common.web.html.BaseDateTdBuilder;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.execution.dto.WfProcess;

/**
 * Created on 16.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class ProcessStartDateTdBuilder extends BaseDateTdBuilder<WfProcess> {

    @Override
    protected Date getDate(WfProcess process) {
        return process.getStartDate();
    }

    @Override
    protected Long getId(WfProcess process) {
        return process.getId();
    }

    @Override
    protected String getActionMapping() {
        return ShowGraphModeHelper.getManageProcessAction();
    }

}
