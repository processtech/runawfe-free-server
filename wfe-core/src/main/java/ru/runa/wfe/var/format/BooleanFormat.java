package ru.runa.wfe.var.format;

public class BooleanFormat extends VariableFormat {

    @Override
    public Class<Boolean> getJavaClass() {
        return Boolean.class;
    }

    @Override
    public String getName() {
        return "boolean";
    }

    @Override
    protected Boolean convertFromStringValue(String source) {
        return "true".equalsIgnoreCase(source) || "on".equalsIgnoreCase(source);
    }

    @Override
    protected String convertToStringValue(Object obj) {
        return obj.toString();
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onBoolean(this, context);
    }
}
