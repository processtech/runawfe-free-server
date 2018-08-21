package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "S")
public class ArchivedTaskEndBySubstitutorLog extends ArchivedTaskEndLog implements TaskEndBySubstitutorLog {
}
