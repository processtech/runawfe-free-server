package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface VariableLog extends ProcessLog {

    @Transient
    String getVariableName();

    @Transient
    String getVariableNewValueAttribute();

    @Transient
    boolean isFileValue();

    @Transient
    boolean isExecutorValue();

    @Transient
    Object getVariableNewValue();

    @Transient
    Object getVariableNewValueForPattern();
}
