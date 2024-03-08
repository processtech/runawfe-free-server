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
    public Object[] getPatternArguments() {
        String reason = getAttribute(ATTR_MESSAGE);
        reason = reason != null ? " (" + reason + ")" : "";
        return new Object[] { new ExecutorNameValue(getExecutorNameNotNull()), reason };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessCancelLog(this);
    }
}
