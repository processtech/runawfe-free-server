package ru.runa.wfe.script.executor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.user.Group;

@XmlType(name = CreateGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class CreateGroupOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "createGroup";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.DESCRIPTION_ATTRIBUTE_NAME, required = false)
    public String description;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        Group newGroup = new Group(name, description);
        context.getExecutorLogic().create(context.getUser(), newGroup);
    }
}
