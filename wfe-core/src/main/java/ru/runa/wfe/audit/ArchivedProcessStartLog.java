package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "I")
public class ArchivedProcessStartLog extends ArchivedProcessLog implements IProcessStartLog {
}
