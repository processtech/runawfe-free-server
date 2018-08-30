package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Z")
public class ArchivedSubprocessEndLog extends ArchivedNodeLeaveLog implements SubprocessEndLog {
}
