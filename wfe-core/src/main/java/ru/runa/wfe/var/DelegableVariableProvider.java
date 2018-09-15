package ru.runa.wfe.var;

import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class DelegableVariableProvider extends AbstractVariableProvider {
    protected final VariableProvider delegate;

    public DelegableVariableProvider(VariableProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Long getDeploymentVersionId() {
        if (delegate != null) {
            return delegate.getDeploymentVersionId();
        }
        return null;
    }

    @Override
    public String getProcessDefinitionName() {
        if (delegate != null) {
            return delegate.getProcessDefinitionName();
        }
        return null;
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition() {
        if (delegate != null) {
            return delegate.getParsedProcessDefinition();
        }
        return null;
    }

    @Override
    public Long getProcessId() {
        if (delegate != null) {
            return delegate.getProcessId();
        }
        return null;
    }

    @Override
    public UserType getUserType(String name) {
        if (delegate != null) {
            return delegate.getUserType(name);
        }
        return null;
    }

    @Override
    public Object getValue(String variableName) {
        if (delegate != null) {
            return delegate.getValue(variableName);
        }
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        if (delegate != null) {
            return delegate.getVariable(variableName);
        }
        return null;
    }

}
