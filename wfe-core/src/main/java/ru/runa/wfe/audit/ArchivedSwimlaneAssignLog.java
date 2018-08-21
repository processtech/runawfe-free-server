package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "4")
public class ArchivedSwimlaneAssignLog extends ArchivedProcessLog implements SwimlaneAssignLog {
}
