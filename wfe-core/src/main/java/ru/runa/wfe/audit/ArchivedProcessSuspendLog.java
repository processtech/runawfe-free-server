package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "V")
public class ArchivedProcessSuspendLog extends ArchivedProcessLog implements ProcessSuspendLog {
}
