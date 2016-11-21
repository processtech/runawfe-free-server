package ru.runa.wfe.script.executor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

import com.google.common.collect.Lists;

@XmlType(name = DeleteExecutorOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class DeleteExecutorOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "deleteExecutor";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        User user = context.getUser();
        ExecutorLogic executorLogic = context.getExecutorLogic();
        executorLogic.remove(user, Lists.newArrayList(executorLogic.getExecutor(user, name).getId()));
    }
}
