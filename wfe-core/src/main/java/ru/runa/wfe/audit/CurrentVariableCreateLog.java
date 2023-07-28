package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Logging variable creation.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "R")
public class CurrentVariableCreateLog extends CurrentVariableLog implements VariableCreateLog {
    private static final long serialVersionUID = 1L;

    public CurrentVariableCreateLog() {
    }

    public CurrentVariableCreateLog(CurrentVariable<?> variable, Object newValue, VariableDefinition variableDefinition) {
        super(variable);
        setVariableNewValue(variable, newValue, variableDefinition);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE_CREATE;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getVariableName(), getVariableNewValueForPattern() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onVariableCreateLog(this);
    }
}
