package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "5")
public class ArchivedTaskEscalationLog extends ArchivedTaskLog implements TaskEscalationLog {
}
