package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "U")
public class ArchivedProcessActivateLog extends ArchivedProcessLog implements ProcessActivateLog {

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_ACTIVATE;
    }
    
    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getExecutorNameNotNull()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessActivateLog(this);
    }
}
