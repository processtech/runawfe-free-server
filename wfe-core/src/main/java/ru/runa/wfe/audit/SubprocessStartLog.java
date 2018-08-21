package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.commons.TypeConversionUtil;

public interface SubprocessStartLog extends NodeEnterLog {

    @Override
    @Transient
    default Type getType() {
        return Type.SUBPROCESS_START;
    }

    @Transient
    default Long getSubprocessId() {
        return TypeConversionUtil.convertTo(Long.class, getAttributeNotNull(ATTR_PROCESS_ID));
    }

    @Transient
    default Long getParentTokenId() {
        return TypeConversionUtil.convertTo(long.class, getAttribute(ATTR_TOKEN_ID));
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { new ProcessIdValue(getSubprocessId()) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onSubprocessStartLog(this);
    }
}
