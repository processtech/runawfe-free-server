package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "K")
public class ArchivedTaskEndByAdminLog extends ArchivedTaskEndLog implements ITaskEndByAdminLog {
}
