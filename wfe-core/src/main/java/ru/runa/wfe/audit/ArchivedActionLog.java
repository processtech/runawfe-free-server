package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "A")
public class ArchivedActionLog extends ArchivedProcessLog implements IActionLog {
}
