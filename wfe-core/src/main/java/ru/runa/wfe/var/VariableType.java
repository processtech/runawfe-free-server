package ru.runa.wfe.var;

import org.springframework.beans.factory.annotation.Required;

/**
 * specifies for one java-type how jbpm is able to persist objects of that type in the database.
 */
public class VariableType {
    private VariableTypeMatcher matcher;
    private Converter converter;
    private Class<? extends CurrentVariable<?>> currentVariableClass;
    private Class<? extends ArchivedVariable<?>> archivedVariableClass;

    public VariableTypeMatcher getMatcher() {
        return matcher;
    }

    @Required
    public void setMatcher(VariableTypeMatcher variableTypeMatcher) {
        this.matcher = variableTypeMatcher;
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Required
    public void setCurrentVariableClass(Class<? extends CurrentVariable<?>> currentVariableClass) {
        this.currentVariableClass = currentVariableClass;
    }

    @Required
    public void setArchivedVariableClass(Class<? extends ArchivedVariable<?>> archivedVariableClass) {
        this.archivedVariableClass = archivedVariableClass;
    }

    public Class<? extends Variable> getVariableClass(boolean isArchive) {
        return isArchive ? archivedVariableClass : currentVariableClass;
    }
}
