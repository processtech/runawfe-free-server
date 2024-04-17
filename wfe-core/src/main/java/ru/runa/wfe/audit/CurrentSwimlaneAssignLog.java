package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.user.Executor;

/**
 * Logging swimlane assignment.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "4")
public class CurrentSwimlaneAssignLog extends CurrentProcessLog implements SwimlaneAssignLog {
    private static final long serialVersionUID = 1L;

    public CurrentSwimlaneAssignLog() {
    }

    public CurrentSwimlaneAssignLog(CurrentSwimlane swimlane, Executor newExecutor) {
        if (swimlane.getExecutor() != null) {
            addAttribute(ATTR_OLD_VALUE, swimlane.getExecutor().getName());
        }
        if (newExecutor != null) {
            addAttribute(ATTR_NEW_VALUE, newExecutor.getName());
        }
        setSeverity(Severity.INFO);
        setSwimlaneName(swimlane.getName());
    }
    
    @Override
    @Transient
    public Type getType() {
        return Type.SWIMLANE_ASSIGN;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getSwimlaneNameNotNull(), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onSwimlaneAssignLog(this);
    }

    @Transient
    private String getSwimlaneNameNotNull() {
        return super.getSwimlaneName() != null ? super.getSwimlaneName() : getAttributeNotNull(ATTR_MESSAGE);
    }

}
