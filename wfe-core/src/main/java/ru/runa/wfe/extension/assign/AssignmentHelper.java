package ru.runa.wfe.extension.assign;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.DelegationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorParticipatesInProcessesException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;

public class AssignmentHelper {
    private static final Log log = LogFactory.getLog(AssignmentHelper.class);

    public static void reassignTask(ExecutionContext executionContext, Task task, Executor newExecutor, boolean reassignSwimlane) {
        Executor oldExecutor = task.getExecutor();
        task.assignExecutor(executionContext, newExecutor, reassignSwimlane);
        removeTemporaryGroupOnTaskEnd(oldExecutor);
    }

    public static void removeTemporaryGroupOnTaskEnd(Executor oldExecutor) {
        if (oldExecutor instanceof TemporaryGroup && SystemProperties.deleteTemporaryGroupsOnTaskEnd()) {
            try {
                log.debug("Cleaning " + oldExecutor);
                ApplicationContextFactory.getExecutorLogic().remove(oldExecutor);
            } catch (ExecutorParticipatesInProcessesException e) {
                // will be removed at process end
                log.debug(e);
            }
        }
    }

    public static boolean assign(ExecutionContext executionContext, Assignable assignable, Collection<? extends Executor> executors) {
        try {
            if (assignable.getExecutor() instanceof DelegationGroup) {
                log.debug("Ignored to assign executors in " + assignable);
                return false;
            }
            if (executors == null || executors.size() == 0) {
                log.warn("Assigning null executor in " + executionContext + ": " + assignable + ", check swimlane initializer");
                assignable.assignExecutor(executionContext, null, true);
                ProcessExecutionException pee = new ProcessExecutionException(assignable.getErrorMessageKey(), assignable.getName());
                ProcessExecutionErrors.addProcessError(executionContext.getProcess().getId(), assignable.getName(), assignable.getName(), null, pee);
                return false;
            }
            ProcessExecutionErrors.removeProcessError(executionContext.getProcess().getId(), assignable.getName());
            if (executors.size() == 1) {
                Executor aloneExecutor = executors.iterator().next();
                assignable.assignExecutor(executionContext, aloneExecutor, true);
                return true;
            }
            String swimlaneName = assignable.getSwimlaneName();
            Group tmpGroup = TemporaryGroup.create(executionContext.getProcess().getId(), swimlaneName);
            tmpGroup = ApplicationContextFactory.getExecutorLogic().saveTemporaryGroup(tmpGroup, executors);
            assignable.assignExecutor(executionContext, tmpGroup, true);
            log.info("Cascaded assignment done in " + assignable);
            return true;
        } catch (Exception e) {
            log.warn("Unable to assign " + assignable + " in " + executionContext.getProcess(), e);
            return false;
        }
    }

}
