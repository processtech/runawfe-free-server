package ru.runa.wfe.ss;

import com.google.common.base.Strings;
import java.util.HashSet;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/**
 * Substitution with this criteria applies when substitutor not in actor list. Actor list contains executors from process variable with 'conf' name.
 */
@Entity
@DiscriminatorValue(value = "not_equals")
public class SubstitutionCriteriaNotEquals extends SubstitutionCriteria {
    private static final long serialVersionUID = 1L;
    private static final String SWIMLANE_PREFIX = "swimlane:";

    @Override
    public boolean isSatisfied(ExecutionContext executionContext, Task task, Actor asActor, Actor substitutorActor) {
        String variableName = getConfiguration();
        Executor executor;
        if (variableName.startsWith(SWIMLANE_PREFIX)) {
            val currentSwimlaneDao = ApplicationContextFactory.getCurrentSwimlaneDao();
            String swimlaneName = variableName.substring(SWIMLANE_PREFIX.length());
            CurrentSwimlane swimlane = currentSwimlaneDao.findByProcessAndName(executionContext.getCurrentProcess(), swimlaneName);
            if (swimlane == null) {
                return true;
            }
            executor = swimlane.getExecutor();
        } else {
            Object variableValue = executionContext.getVariableProvider().getValue(variableName);
            if (variableValue == null) {
                return true;
            }
            executor = TypeConversionUtil.convertTo(Executor.class, variableValue);
        }
        val confActors = new HashSet<Executor>();
        if (executor instanceof Group) {
            confActors.addAll(ApplicationContextFactory.getExecutorDao().getGroupActors((Group) executor));
        } else {
            confActors.add(executor);
        }
        return !confActors.contains(substitutorActor);
    }

    @Override
    public void validate() {
        if (Strings.isNullOrEmpty(getConfiguration())) {
            throw new InternalApplicationException(getClass().getName() + ": invalid configuration");
        }
    }
}
