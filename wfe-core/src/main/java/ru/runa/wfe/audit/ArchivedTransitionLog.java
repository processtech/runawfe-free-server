package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "T")
public class ArchivedTransitionLog extends ArchivedProcessLog implements ITransitionLog {
}
