package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "L")
public class ArchivedNodeLeaveLog extends ArchivedNodeLog implements INodeLeaveLog {
}
