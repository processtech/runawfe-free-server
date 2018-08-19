package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "E")
public class ArchivedAdminActionLog extends ArchivedProcessLog implements IAdminActionLog {
}
