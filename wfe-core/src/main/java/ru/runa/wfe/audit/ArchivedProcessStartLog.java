package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "I")
public class ArchivedProcessStartLog extends ArchivedProcessLog implements ProcessStartLog {

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_START;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getExecutorNameNotNull()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessStartLog(this);
    }
}
