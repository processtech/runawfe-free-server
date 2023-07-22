package ru.runa.wfe.office.storage.binding;

public class ExecutionResult {

    private Object value;
    private boolean needReturn = false;

    public static final ExecutionResult EMPTY = new ExecutionResult();

    private ExecutionResult() {
    }

    public ExecutionResult(Object value) {
        this.value = value;
        this.needReturn = true;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isNeedReturn() {
        return needReturn;
    }

    public void setNeedReturn(boolean needReturn) {
        this.needReturn = needReturn;
    }

}
