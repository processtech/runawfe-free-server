package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.user.Executor;

/**
 * Logging swimlane assignment.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "4")
public class SwimlaneAssignLog extends ProcessLog {
    private static final long serialVersionUID = 1L;

    public SwimlaneAssignLog() {
    }

    public SwimlaneAssignLog(Swimlane swimlane, Executor newExecutor) {
        addAttribute(ATTR_MESSAGE, swimlane.getName());
        if (swimlane.getExecutor() != null) {
            addAttribute(ATTR_OLD_VALUE, swimlane.getExecutor().getName());
        }
        if (newExecutor != null) {
            addAttribute(ATTR_NEW_VALUE, newExecutor.getName());
        }
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_MESSAGE), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onSwimlaneAssignLog(this);
    }
}
