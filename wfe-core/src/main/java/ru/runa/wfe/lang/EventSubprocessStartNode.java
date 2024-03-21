package ru.runa.wfe.lang;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentNodeEnterLog;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.execution.EventSubprocessTrigger;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.EventSubprocessTriggerDao;
import ru.runa.wfe.job.StartEventSubprocessTimerJob;
import ru.runa.wfe.job.dao.TimerJobDao;
import ru.runa.wfe.job.impl.TimerJobFactory;

public class EventSubprocessStartNode extends EmbeddedSubprocessStartNode {
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient EventSubprocessTriggerDao eventSubprocessTriggerDao;
    @Autowired
    private transient TimerJobFactory timerJobFactory;
    @Autowired
    private transient TimerJobDao timerJobDao;

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
            StartEventSubprocessTimerJob job = timerJobFactory.createStartEventTimerJobFromTimerEventDefinition(getTimerEventDefinition(),
                    executionContext.getCurrentProcess(), getNodeId(), getName());
            if (job != null) {
                timerJobDao.create(job);
                executionContext.addLog(new CurrentNodeEnterLog(getSubprocessNode()));
            }
        }
    }

}
