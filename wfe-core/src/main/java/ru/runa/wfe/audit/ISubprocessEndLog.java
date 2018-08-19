package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.commons.TypeConversionUtil;

public interface ISubprocessEndLog extends INodeLeaveLog {

    @Transient
    default Long getSubprocessId() {
        return TypeConversionUtil.convertTo(Long.class, getAttributeNotNull(ATTR_PROCESS_ID));
    }

    @Transient
    Long getParentTokenId();

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { new ProcessIdValue(getSubprocessId()) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onSubprocessEndLog(this);
    }
}
