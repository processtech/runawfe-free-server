package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "C")
@SuppressWarnings("unused")
public class ArchivedCreateTimerLog extends ArchivedProcessLog implements CreateTimerLog {
}
