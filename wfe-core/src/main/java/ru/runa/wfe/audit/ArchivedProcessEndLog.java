package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "X")
public class ArchivedProcessEndLog extends ArchivedProcessLog implements ProcessEndLog {
}
