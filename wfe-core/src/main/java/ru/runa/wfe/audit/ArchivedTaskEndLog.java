package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "3")
public class ArchivedTaskEndLog extends ArchivedTaskLog implements TaskEndLog {
}
