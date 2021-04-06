package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.commons.TypeConversionUtil;

@Entity
@DiscriminatorValue(value = "B")
public class ArchivedSubprocessStartLog extends ArchivedNodeEnterLog implements SubprocessStartLog {

    @Override
    @Transient
    public Type getType() {
        return Type.SUBPROCESS_START;
    }

    @Override
    @Transient
    public Long getSubprocessId() {
        return TypeConversionUtil.convertTo(Long.class, getAttributeNotNull(ATTR_PROCESS_ID));
    }

    @Override
    @Transient
    public Long getParentTokenId() {
        return TypeConversionUtil.convertTo(long.class, getAttribute(ATTR_TOKEN_ID));
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ProcessIdValue(getSubprocessId()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onSubprocessStartLog(this);
    }
}
