package ru.runa.wfe.lang.jpdl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentActionLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.dao.TimerJobDao;
import ru.runa.wfe.lang.Action;

public class CancelTimerAction extends Action {
    private static final long serialVersionUID = 1L;
    @Autowired
    private transient TimerJobDao timerJobDao;

    @Override
    public void execute(ExecutionContext executionContext) {
        timerJobDao.deleteByToken(executionContext.getCurrentToken());
        executionContext.addLog(new CurrentActionLog(this));
    }
}
