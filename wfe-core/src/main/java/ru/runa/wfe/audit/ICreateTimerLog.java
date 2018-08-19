package ru.runa.wfe.audit;

public interface ICreateTimerLog extends IProcessLog {

    @Override
    default Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_DUE_DATE) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onCreateTimerLog(this);
    }
}
