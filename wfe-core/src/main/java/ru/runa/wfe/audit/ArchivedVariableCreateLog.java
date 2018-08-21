package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "R")
public class ArchivedVariableCreateLog extends ArchivedVariableLog implements VariableCreateLog {
}
