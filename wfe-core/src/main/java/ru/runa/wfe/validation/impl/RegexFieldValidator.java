package ru.runa.wfe.validation.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.validation.FieldValidator;

import com.google.common.base.Strings;

public class RegexFieldValidator extends FieldValidator {

    protected String getExpression() {
        return getParameterNotNull(String.class, "expression");
    }

    protected boolean isCaseSensitive() {
        return getParameter(boolean.class, "caseSensitive", true);
    }

    @Override
    public void validate() {
        String value = TypeConversionUtil.convertTo(String.class, getFieldValue());
        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the
        // field
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        String expression = getExpression();
        boolean caseSensitive = isCaseSensitive();
        boolean trim = getParameter(boolean.class, "trim", true);

        // match against expression
        Pattern pattern;
        if (caseSensitive) {
            pattern = Pattern.compile(expression);
        } else {
            pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        }

        String compare = value;
        if (trim) {
            compare = compare.trim();
        }
        Matcher matcher = pattern.matcher(compare);

        if (!matcher.matches()) {
            addError();
        }
    }

}
