package ru.runa.wfe.var.matcher;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.var.VariableTypeMatcher;

public class ClassNameMatcher implements VariableTypeMatcher {
    private Class<?> baseClass;

    @Required
    public void setClassName(String className) {
        baseClass = ClassLoaderUtil.loadClass(className);
    }

    @Override
    public boolean matches(Object value) {
        return baseClass.isAssignableFrom(value.getClass());
    }
}
