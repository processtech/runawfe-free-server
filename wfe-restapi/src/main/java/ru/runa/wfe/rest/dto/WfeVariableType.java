package ru.runa.wfe.rest.dto;

import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.file.FileVariable;



public enum WfeVariableType {
    STRING(String.class),
    DATE(Date.class),
    BOOLEAN(Boolean.class),
    NUMBER(Long.class, Double.class, BigDecimal.class),
    FILE(FileVariable.class),
    USER_TYPE(UserTypeMap.class),
    LIST(List.class),
    EXECUTOR(Executor.class, Actor.class, Group.class);

    private final Set<Class<?>> javaClasses;

    private WfeVariableType(Class<?>... javaClasses) {
        this.javaClasses = Sets.newHashSet(javaClasses);
    }

    public static WfeVariableType findByJavaClass(Class<?> javaClass) {
        for (WfeVariableType type : values()) {
            if (type.javaClasses.contains(javaClass)) {
                return type;
            }
        }
        return null;

    }
}
