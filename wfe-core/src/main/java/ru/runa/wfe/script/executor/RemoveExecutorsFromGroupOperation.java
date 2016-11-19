package ru.runa.wfe.script.executor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ExecutorsSetContainerOperation;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.user.Group;

@XmlType(name = RemoveExecutorsFromGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemoveExecutorsFromGroupOperation extends ExecutorsSetContainerOperation {

    public static final String SCRIPT_NAME = "removeExecutorsFromGroup";

    /**
     * Group name to remove executors.
     */
    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        super.validate(true);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        Group group = context.getExecutorLogic().getGroup(context.getUser(), name);
        context.getExecutorLogic().removeExecutorsFromGroup(context.getUser(), getExecutors(context), group);
    }
}
