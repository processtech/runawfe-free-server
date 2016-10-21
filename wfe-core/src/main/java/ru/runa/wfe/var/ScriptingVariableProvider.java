package ru.runa.wfe.var;


public class ScriptingVariableProvider extends DelegableVariableProvider {

    public ScriptingVariableProvider(IVariableProvider variableProvider) {
        super(variableProvider);
    }

    @Override
    public Object getValue(String variableName) {
        Object object = super.getValue(variableName);
        if (object instanceof UserTypeMap) {
            object = new ScriptingUserTypeMap((UserTypeMap) object);
        }
        return object;
    }

}
