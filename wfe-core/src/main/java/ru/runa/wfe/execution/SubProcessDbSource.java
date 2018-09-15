package ru.runa.wfe.execution;

import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;

class SubProcessDbSource extends DefaultDbSource {
    public SubProcessDbSource(Class<?> sourceObject, String valueDBPath) {
        super(sourceObject, valueDBPath);
    }

    @Override
    public String getJoinExpression(String alias) {
        return "CAST(" + ClassPresentation.classNameSQL + ".id AS VARCHAR(128))" + " = " + alias + ".hierarchyIds";
    }
}