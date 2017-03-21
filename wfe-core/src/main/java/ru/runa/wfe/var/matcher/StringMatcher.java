package ru.runa.wfe.var.matcher;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableTypeMatcher;

public class StringMatcher implements VariableTypeMatcher {
    private boolean large;

    @Required
    public void setLarge(boolean large) {
        this.large = large;
    }

    @Override
    public boolean matches(Object value) {
        if (value.getClass() != String.class) {
            return false;
        }
        int len = ((String) value).length();
        if (large) {
            return len > Variable.getMaxStringSize();
        }
        return len <= Variable.getMaxStringSize();
    }

}
