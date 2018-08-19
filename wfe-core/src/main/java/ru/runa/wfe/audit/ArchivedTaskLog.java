package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "0")
public abstract class ArchivedTaskLog extends ArchivedProcessLog implements ITaskLog {
}
