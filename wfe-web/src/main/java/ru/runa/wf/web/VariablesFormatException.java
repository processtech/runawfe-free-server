package ru.runa.wf.web;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public class VariablesFormatException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final List<String> errorFields = Lists.newArrayList();

    public VariablesFormatException(Collection<String> errorFields) {
        this.errorFields.addAll(errorFields);
    }

    public List<String> getErrorFields() {
        return errorFields;
    }
}
