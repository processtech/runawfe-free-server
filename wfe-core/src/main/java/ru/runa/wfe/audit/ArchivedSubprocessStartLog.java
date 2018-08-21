package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "B")
public class ArchivedSubprocessStartLog extends ArchivedNodeEnterLog implements SubprocessStartLog {
}
