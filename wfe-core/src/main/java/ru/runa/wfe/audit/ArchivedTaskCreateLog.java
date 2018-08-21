package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "1")
public class ArchivedTaskCreateLog extends ArchivedTaskLog implements TaskCreateLog {
}
