package ru.runa.wfe.execution;

import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;

class SubProcessDBSource extends DefaultDBSource {
    public SubProcessDBSource(Class<?> sourceObject, String valueDBPath) {
        super(sourceObject, valueDBPath);
    }

    @Override
    public String getJoinExpression(String alias) {
        return "CAST(" + ClassPresentation.classNameSQL + ".id AS VARCHAR(128))" + " = " + alias + ".hierarchyIds";
    }
}