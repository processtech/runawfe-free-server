package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "8")
public class ArchivedReceiveMessageLog extends ArchivedNodeEnterLog implements ReceiveMessageLog {
}
