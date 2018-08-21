package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "M")
public class ArchivedTaskRemovedOnProcessEndLog extends ArchivedTaskEndLog implements TaskRemovedOnProcessEndLog {
}
