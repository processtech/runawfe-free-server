package ru.runa.wfe.var.logic;

public class ByReferenceWriteResult {

    static final ByReferenceWriteResult SKIP = new ByReferenceWriteResult(false, null);

    public final boolean shouldSave;
    public final Object value;

    private ByReferenceWriteResult(boolean shouldSave, Object value) {
        this.shouldSave = shouldSave;
        this.value = value;
    }

    static ByReferenceWriteResult save(Object value) {
        return new ByReferenceWriteResult(true, value);
    }
}
