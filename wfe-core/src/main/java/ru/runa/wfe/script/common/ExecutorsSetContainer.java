package ru.runa.wfe.script.common;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

@XmlType(name = "ExecutorsSetContainerType", namespace = AdminScriptConstants.NAMESPACE)
public class ExecutorsSetContainer extends IdentitiesSetContainer {

    @XmlElement(name = AdminScriptConstants.EXECUTOR_ELEMENT_NAME, namespace = AdminScriptConstants.NAMESPACE)
    public List<Identity> executors = Lists.newArrayList();

    public ExecutorsSetContainer() {
        super(NamedIdentityType.EXECUTOR);
    }

    @Override
    public void validate(ScriptOperation scriptOperation, boolean requiredNotEmpty) {
        super.validate(scriptOperation, false);
        if (requiredNotEmpty) {
            if (executors.size() == 0 && !super.isStandartIdentitiesSetDefined()) {
                throw new ScriptValidationException(scriptOperation, "Required " + AdminScriptConstants.EXECUTOR_ELEMENT_NAME + " or "
                        + AdminScriptConstants.NAMED_IDENTITY_ELEMENT_NAME + " or " + AdminScriptConstants.IDENTITY_ELEMENT_NAME + " elements.");
            }
        }
    }

    @Override
    public boolean isStandartIdentitiesSetDefined() {
        return super.isStandartIdentitiesSetDefined() || executors.size() != 0;
    }

    @Override
    public Set<String> getIdentityNames(ScriptExecutionContext context) {
        Set<String> executorNames = super.getIdentityNames(context);
        for (Identity op : executors) {
            executorNames.add(op.name);
        }
        return executorNames;
    }

    public List<Executor> getExecutors(ScriptExecutionContext context) {
        List<Executor> executors = Lists.newArrayList();
        for (String executorName : getIdentityNames(context)) {
            executors.add(context.getExecutorLogic().getExecutor(context.getUser(), executorName));
        }
        return executors;
    }
}
