package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "D")
public class ArchivedVariableDeleteLog extends ArchivedVariableLog implements VariableDeleteLog {
}
