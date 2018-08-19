package ru.runa.wfe.audit;

public interface IActionLog extends IProcessLog {

    @Override
    default Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_ACTION) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onActionLog(this);
    }
}
