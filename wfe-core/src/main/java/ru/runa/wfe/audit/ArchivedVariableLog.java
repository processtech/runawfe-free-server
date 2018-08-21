package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "0")
public abstract class ArchivedVariableLog extends ArchivedProcessLog implements VariableLog {

    @Override
    public void setVariableName(String variableName) {
        throw new IllegalAccessError();
    }
}
