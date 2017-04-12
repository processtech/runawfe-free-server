package ru.runa.wfe.presentation;

/**
 * Implementation of {@link DBSource} interface for referencing variable values.
 */
public class VariableDBSource extends DefaultDBSource {
    public VariableDBSource(Class<?> sourceObject) {
        super(sourceObject, "stringValue");
    }

    @Override
    public String getJoinExpression(String alias) {
        return ClassPresentation.classNameSQL + ".id=" + alias + ".process";
    }
}