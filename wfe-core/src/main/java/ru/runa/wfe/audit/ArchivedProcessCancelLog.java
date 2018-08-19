package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Y")
public class ArchivedProcessCancelLog extends ArchivedProcessLog implements IProcessCancelLog {
}
