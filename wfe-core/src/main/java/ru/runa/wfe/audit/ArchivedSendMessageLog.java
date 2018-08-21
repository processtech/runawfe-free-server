package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "7")
public class ArchivedSendMessageLog extends ArchivedNodeEnterLog implements SendMessageLog {
}
