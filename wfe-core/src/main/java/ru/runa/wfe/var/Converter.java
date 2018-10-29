package ru.runa.wfe.var;

import java.io.Serializable;
import ru.runa.wfe.execution.ExecutionContext;

/**
 * converts plain objects to objects that are persistable via a subclass of Variable.
 */
public interface Converter extends Serializable {

    /**
     * is true if this converter supports the given type, false otherwise.
     */
    boolean supports(Object value);

    /**
     * converts a given object to its persistable format.
     */
    Object convert(ExecutionContext executionContext, Variable variable, Object o);

    /**
     * reverts a persisted object to its original form.
     */
    Object revert(Object o);
}
