package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "Z")
public class ArchivedSubprocessEndLog extends ArchivedNodeLeaveLog implements ISubprocessEndLog {

    @Override
    @Transient
    public Long getParentTokenId() {
        return null;
    }
}
