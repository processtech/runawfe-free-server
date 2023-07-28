package ru.runa.wf.logic.bot.updatepermission;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import ru.runa.wfe.security.Permission;

public class UpdatePermissionsSettings {
    private final Collection<Permission> permissions = Lists.newArrayList();
    private final List<String> swimlaneInitializers = Lists.newArrayList();
    private Method method;
    private String conditionVarName;
    private String conditionVarValue;

    public void setCondition(String conditionVarName, String conditionVarValue) {
        this.conditionVarName = conditionVarName;
        this.conditionVarValue = conditionVarValue;
    }

    public boolean isConditionExists() {
        return !Strings.isNullOrEmpty(conditionVarName);
    }

    public String getConditionVarName() {
        return conditionVarName;
    }

    public String getConditionVarValue() {
        return conditionVarValue;
    }

    public List<String> getSwimlaneInitializers() {
        return swimlaneInitializers;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
