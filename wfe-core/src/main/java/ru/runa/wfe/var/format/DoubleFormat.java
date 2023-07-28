package ru.runa.wfe.var.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class DoubleFormat extends VariableFormat {

    @Override
    public Class<Double> getJavaClass() {
        return Double.class;
    }

    @Override
    public String getName() {
        return "double";
    }

    @Override
    protected Double convertFromStringValue(String source) {
        return Double.valueOf(source);
    }

    @Override
    protected String convertToStringValue(Object obj) {
        DecimalFormat format = new DecimalFormat("0.#");
        format.setMaximumFractionDigits(340);
        DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(dfs);
        return format.format(obj);
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onDouble(this, context);
    }

}
