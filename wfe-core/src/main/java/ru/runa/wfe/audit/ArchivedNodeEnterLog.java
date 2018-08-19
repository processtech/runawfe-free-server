package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "N")
public class ArchivedNodeEnterLog extends ArchivedNodeLog implements INodeEnterLog {
}
