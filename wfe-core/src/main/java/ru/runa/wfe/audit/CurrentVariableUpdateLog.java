package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Logging variable update.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "W")
public class CurrentVariableUpdateLog extends CurrentVariableLog implements VariableUpdateLog {
    private static final long serialVersionUID = 1L;

    public CurrentVariableUpdateLog() {
    }

    public CurrentVariableUpdateLog(CurrentVariable<?> variable, Object newValue, VariableDefinition variableDefinition) {
        super(variable);
        setVariableNewValue(variable, newValue, variableDefinition);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE_UPDATE;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getVariableNameNotNull(), getVariableNewValueForPattern() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableUpdateLog(this);
    }
}
