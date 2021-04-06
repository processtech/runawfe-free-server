package ru.runa.wfe.var.format;

/**
 * Created on 22.05.2006
 *
 */
public class LongFormat extends VariableFormat {

    @Override
    public Class<? extends Number> getJavaClass() {
        return Long.class;
    }

    @Override
    public String getName() {
        return "integer";
    }

    @Override
    protected Long convertFromStringValue(String source) {
        return Long.valueOf(source);
    }

    @Override
    protected String convertToStringValue(Object obj) {
        return obj.toString();
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onLong(this, context);
    }

}
