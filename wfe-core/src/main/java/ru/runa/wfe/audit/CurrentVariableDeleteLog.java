package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.var.CurrentVariable;

/**
 * Logging variable deletion.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "D")
public class CurrentVariableDeleteLog extends CurrentVariableLog implements VariableDeleteLog {
    private static final long serialVersionUID = 1L;

    public CurrentVariableDeleteLog() {
    }

    public CurrentVariableDeleteLog(CurrentVariable<?> variable) {
        super(variable);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE_DELETE;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getVariableName() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableDeleteLog(this);
    }
}
