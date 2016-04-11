package ru.runa.wfe.var.format;

import java.math.BigDecimal;

/**
 * BigDecimal variable support.
 * 
 * @author dofs
 * @since 4.0
 */
public class BigDecimalFormat extends VariableFormat {

    @Override
    public Class<BigDecimal> getJavaClass() {
        return BigDecimal.class;
    }

    @Override
    public String getName() {
        return "bigdecimal";
    }

    @Override
    protected BigDecimal convertFromStringValue(String source) {
        return new BigDecimal(source);
    }

    @Override
    protected String convertToStringValue(Object obj) {
        return obj.toString();
    }

}
