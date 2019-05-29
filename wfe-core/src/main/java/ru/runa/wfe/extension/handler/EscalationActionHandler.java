package ru.runa.wfe.extension.handler;

import com.google.common.base.Strings;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentTaskEscalationLog;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.RelationSwimlaneInitializer;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.lang.TaskNode;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.user.logic.ExecutorLogic;

public class EscalationActionHandler extends ActionHandlerBase {
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private ExecutorLogic executorLogic;
    private String hierarchyLoader;

    @Override
    public void setConfiguration(String configuration) {
        super.setConfiguration(configuration);
        if (Strings.isNullOrEmpty(configuration)) {
            this.hierarchyLoader = SystemProperties.getEscalationDefaultHierarchyLoader();
        } else {
            this.hierarchyLoader = configuration;
        }
        if (!RelationSwimlaneInitializer.isValid(hierarchyLoader)) {
            ClassLoaderUtil.instantiate(hierarchyLoader);
        }
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        if (!SystemProperties.isEscalationEnabled()) {
            log.info("Escalation disabled");
            executionContext.setTransientVariable(TimerJob.STOP_RE_EXECUTION, Boolean.TRUE);
            return;
        }
        if (executionContext.getNode() instanceof TaskNode) {
            Task task = executionContext.getTask();
            if (task == null) {
                log.error("Task is null in " + executionContext);
                return;
            }
            Executor taskExecutor = task.getExecutor();
            if (taskExecutor == null) {
                log.warn("Task is not assigned: " + task + ", swimlane = " + task.getSwimlane());
                return;
            }
            log.info("Escalation for " + task + " with " + task.getSwimlane());
            Executor originalExecutor;
            int previousEscalationLevel = 0;
            Set<Actor> previousSwimlaneActors = new HashSet<Actor>();

            if (taskExecutor instanceof Group) {
                Group swimlaneGroup = (Group) taskExecutor;
                if (swimlaneGroup instanceof EscalationGroup) {
                    EscalationGroup escalationGroup = (EscalationGroup) swimlaneGroup;
                    originalExecutor = escalationGroup.getOriginalExecutor();
                    previousEscalationLevel = escalationGroup.getLevel();
                } else {
                    originalExecutor = swimlaneGroup;
                }
                for (Executor executor : executorDao.getGroupChildren(swimlaneGroup)) {
                    if (executor instanceof Actor) {
                        previousSwimlaneActors.add((Actor) executor);
                    } else {
                        log.warn("Unexpected: group in TmpGroup: " + executor);
                    }
                }
            } else {
                Actor swimlaneActor = (Actor) taskExecutor;
                originalExecutor = swimlaneActor;
                previousSwimlaneActors.add(swimlaneActor);
            }

            Set<Executor> assignedExecutors = new HashSet<Executor>();
            assignedExecutors.addAll(previousSwimlaneActors);
            for (Actor previousActor : previousSwimlaneActors) {
                String swimlaneInitializer = hierarchyLoader + "(";
                if (RelationSwimlaneInitializer.isValid(swimlaneInitializer)) {
                    swimlaneInitializer += RelationSwimlaneInitializer.RELATION_PARAM_VALUE;
                }
                swimlaneInitializer += previousActor.getCode() + ")";
                List<? extends Executor> executors = SwimlaneInitializerHelper.evaluate(swimlaneInitializer, null);
                if (executors.size() == 0) {
                    log.debug("No escalation will be done for member: " + swimlaneInitializer);
                } else {
                    for (Executor functionExecutor : executors) {
                        assignedExecutors.add(functionExecutor);
                    }
                }
            }
            if (assignedExecutors.size() == previousSwimlaneActors.size()) {
                log.debug("Escalation ignored. No new members found for " + previousSwimlaneActors);
                executionContext.setTransientVariable(TimerJob.STOP_RE_EXECUTION, Boolean.TRUE);
                return;
            }
            int escalationLevel = previousEscalationLevel + 1;
            CurrentProcess process = executionContext.getCurrentProcess();
            Group escalationGroup = EscalationGroup.create(process, task, originalExecutor, escalationLevel);
            escalationGroup = executorLogic.saveTemporaryGroup(escalationGroup, assignedExecutors);
            executionContext.addLog(new CurrentTaskEscalationLog(task, assignedExecutors));
            task.assignExecutor(executionContext, escalationGroup, false);
        } else {
            log.error("Incorrect NodeType for escalation: " + executionContext.getNode());
        }
    }
}
