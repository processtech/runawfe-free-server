package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "F")
public class ArchivedTaskDelegationLog extends ArchivedTaskLog implements ITaskDelegationLog {
}
