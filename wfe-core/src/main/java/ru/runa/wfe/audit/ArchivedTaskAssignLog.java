package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "2")
public class ArchivedTaskAssignLog extends ArchivedTaskLog implements TaskAssignLog {
}
