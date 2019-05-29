package ru.runa.wfe.ss;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Strings;

/**
 * Substitution with this criteria applies when task process swimlane equals
 * configuration.
 */
@Entity
@DiscriminatorValue(value = "swimlane")
public class SubstitutionCriteriaSwimlane extends SubstitutionCriteria {
    private static final long serialVersionUID = 812323181231L;

    @Override
    public boolean isSatisfied(ExecutionContext executionContext, Task task, Actor asActor, Actor substitutorActor) {
        String processDefinitionName = executionContext.getParsedProcessDefinition().getName();
        if (task.getSwimlane() == null) {
            return false;
        }
        String taskSwimlaneName = task.getSwimlane().getName();
        String expectedSwimlaneName = processDefinitionName + "." + taskSwimlaneName;
        return expectedSwimlaneName.equals(getConfiguration());
    }

    @Override
    public void validate() {
        if (Strings.isNullOrEmpty(getConfiguration())) {
            throw new InternalApplicationException(getClass().getName() + ": invalid configuration");
        }
    }

}
