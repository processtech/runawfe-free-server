package ru.runa.wfe.var.matcher;

import java.io.Serializable;

import ru.runa.wfe.var.VariableTypeMatcher;

public class SerializableMatcher implements VariableTypeMatcher {

    @Override
    public boolean matches(Object value) {
        return value instanceof Serializable;
    }

}
