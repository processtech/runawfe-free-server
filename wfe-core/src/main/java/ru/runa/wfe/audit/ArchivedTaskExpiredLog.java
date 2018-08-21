package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "9")
public class ArchivedTaskExpiredLog extends ArchivedTaskEndLog implements TaskExpiredLog {
}
