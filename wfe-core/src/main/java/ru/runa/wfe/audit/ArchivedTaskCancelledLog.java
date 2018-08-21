package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "O")
public class ArchivedTaskCancelledLog extends ArchivedTaskEndLog implements TaskCancelledLog {
}
