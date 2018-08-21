package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "W")
public class ArchivedVariableUpdateLog extends ArchivedVariableLog implements VariableUpdateLog {
}
