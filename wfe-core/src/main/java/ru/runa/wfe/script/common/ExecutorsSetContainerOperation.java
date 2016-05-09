package ru.runa.wfe.script.common;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

@XmlTransient()
public abstract class ExecutorsSetContainerOperation extends IdentitiesSetContainerOperation {

    @XmlElement(name = AdminScriptConstants.EXECUTOR_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<Identity> executors = Lists.newArrayList();

    public ExecutorsSetContainerOperation() {
        super(NamedIdentityType.EXECUTOR);
    }

    @Override
    protected void validate(boolean requiredNotEmpty) {
        super.validate(false);
        if (requiredNotEmpty) {
            if (executors.size() == 0 && !super.isStandartIdentitiesSetDefined()) {
                throw new ScriptValidationException(this, "Required " + AdminScriptConstants.EXECUTOR_ELEMENT_NAME + " or "
                        + AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME + " elements.");
            }
        }
    }

    @Override
    protected boolean isStandartIdentitiesSetDefined() {
        return super.isStandartIdentitiesSetDefined() || executors.size() != 0;
    }

    @Override
    protected Set<String> getIdentityNames(ScriptExecutionContext context) {
        Set<String> executorNames = super.getIdentityNames(context);
        for (Identity op : executors) {
            executorNames.add(op.name);
        }
        return executorNames;
    }

    protected List<Executor> getExecutors(ScriptExecutionContext context) {
        List<Executor> executors = Lists.newArrayList();
        for (String executorName : getIdentityNames(context)) {
            executors.add(context.getExecutorLogic().getExecutor(context.getUser(), executorName));
        }
        return executors;
    }
}
