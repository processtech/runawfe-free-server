package ru.runa.wfe.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ValidatorContext {
    private Collection<String> globalErrors = Lists.newArrayList();
    private Map<String, List<String>> fieldErrors = Maps.newHashMap();

    public Collection<String> getGlobalErrors() {
        return globalErrors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void addGlobalError(String anErrorMessage) {
        globalErrors.add(anErrorMessage);
    }

    public void addFieldError(String fieldName, String errorMessage) {
        List<String> thisFieldErrors = fieldErrors.get(fieldName);
        if (thisFieldErrors == null) {
            thisFieldErrors = new ArrayList<String>();
            fieldErrors.put(fieldName, thisFieldErrors);
        }
        thisFieldErrors.add(errorMessage);
    }

    public boolean hasGlobalErrors() {
        return !globalErrors.isEmpty();
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

}
