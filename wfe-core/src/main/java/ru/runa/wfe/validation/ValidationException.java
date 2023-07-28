package ru.runa.wfe.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Thrown if variables validation fails against configured validators.
 * 
 * @author Dofs
 * @since 3.0
 */
public class ValidationException extends InternalApplicationException {
    private static final long serialVersionUID = 5L;
    private final HashMap<String, List<String>> fieldErrors = Maps.newHashMap();
    private final List<String> globalErrors = Lists.newArrayList();

    public ValidationException() {
    }

    public ValidationException(Map<String, List<String>> fieldErrors, Collection<String> globalErrors) {
        super("Global: " + globalErrors + ", field: " + fieldErrors);
        this.fieldErrors.putAll(fieldErrors);
        this.globalErrors.addAll(globalErrors);
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public Collection<String> getGlobalErrors() {
        return globalErrors;
    }

    public HashMap<String, String> getConcatenatedFieldErrors(String delimiter) {
        HashMap<String, String> concatenated = Maps.newHashMap();
        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
            String concat = Joiner.on(delimiter).join(entry.getValue());
            concatenated.put(entry.getKey(), concat);
        }
        return concatenated;
    }

}
