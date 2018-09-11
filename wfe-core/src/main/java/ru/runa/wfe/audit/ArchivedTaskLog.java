package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = ".")
public abstract class ArchivedTaskLog extends ArchivedProcessLog implements TaskLog {
}
