package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "U")
public class ArchivedProcessActivateLog extends ArchivedProcessLog implements IProcessActivateLog {
}
