package ru.runa.wfe.lang;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentNodeEnterLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.EventSubprocessTrigger;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.dao.EventSubprocessTriggerDao;
import ru.runa.wfe.job.StartProcessTimerJob;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.job.impl.TimerJobFactory;

public class EventSubprocessStartNode extends EmbeddedSubprocessStartNode {
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient EventSubprocessTriggerDao eventSubprocessTriggerDao;
    @Autowired
    private transient TimerJobFactory timerJobFactory;
    @Autowired
    private transient JobDao jobDao;

    public String getParentSubprocessDefinitionIdOrNull() {
        if (getSubprocessNode().getNodeId().startsWith(FileDataProvider.SUBPROCESS_DEFINITION_PREFIX)) {
            int index = getSubprocessNode().getNodeId().indexOf(".");
            return getSubprocessNode().getNodeId().substring(0, index);
        }
        return null;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        if (getTimerEventDefinition() == null) {
            String messageSelector = Utils.getStartNodeMessageSelector(executionContext.getVariableProvider(), this);
            eventSubprocessTriggerDao.create(new EventSubprocessTrigger(executionContext.getCurrentProcess(), this, messageSelector));
        } else {
            StartProcessTimerJob job = timerJobFactory.createTimerJobFromTimerEventDefinition(getTimerEventDefinition(),
                    getParsedProcessDefinition().getId());
            if (job != null) {
                CurrentToken timerToken = new CurrentToken(executionContext.getParsedProcessDefinition(), executionContext.getCurrentProcess(), this);
                ApplicationContextFactory.getCurrentTokenDao().create(timerToken);
                job.setToken(timerToken);
                jobDao.create(job);
            }
        }
    }

    public void onTimerJob(ExecutionContext executionContext, Long remainingCount) {
        if (executionContext.getToken().getExecutionStatus() != ExecutionStatus.ACTIVE) {
            return;
        }
        if (remainingCount != null && remainingCount == 1) {
            super.leave(executionContext);
            return;
        }
        leave(executionContext);
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        executionContext.addLog(new CurrentNodeEnterLog(getSubprocessNode()));
        CurrentToken eventToken = new CurrentToken(executionContext.getParsedProcessDefinition(), executionContext.getCurrentProcess(), this);
        ApplicationContextFactory.getCurrentTokenDao().create(eventToken);
        super.leave(new ExecutionContext(executionContext.getParsedProcessDefinition(), eventToken), transition);
    }
}
