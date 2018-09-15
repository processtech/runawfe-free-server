package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "Y")
public class ArchivedProcessCancelLog extends ArchivedProcessLog implements ProcessCancelLog {

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_CANCEL;
    }

    @Override
    @Transient
    public String getActorName() {
        return getAttribute(ATTR_ACTOR_NAME);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getAttributeNotNull(ATTR_ACTOR_NAME)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessCancelLog(this);
    }
}
