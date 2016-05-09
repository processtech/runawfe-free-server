package ru.runa.wfe.script.executor;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.script.common.ExecutorsSetContainerOperation;
import ru.runa.wfe.script.common.ScriptExecutionContext;

import com.google.common.collect.Lists;

@XmlType(name = DeleteExecutorsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class DeleteExecutorsOperation extends ExecutorsSetContainerOperation {

    public static final String SCRIPT_NAME = "deleteExecutors";

    @XmlElement(name = DeleteExecutorOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, required = true)
    public List<DeleteExecutorOperation> deleteExecutors = Lists.newArrayList();

    @Override
    public void validate(ScriptExecutionContext context) {
        super.validate(false);
        if (deleteExecutors.size() == 0 && !super.isStandartIdentitiesSetDefined()) {
            throw new AdminScriptException(SCRIPT_NAME + " must contains " + DeleteExecutorOperation.SCRIPT_NAME
                    + " or standart executor set definition elements.");
        }
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        Set<String> executorNames = getIdentityNames(context);
        for (DeleteExecutorOperation op : deleteExecutors) {
            executorNames.add(op.name);
        }
        List<Long> executorIds = Lists.newArrayList();
        for (String name : executorNames) {
            executorIds.add(context.getExecutorLogic().getExecutor(context.getUser(), name).getId());
        }
        context.getExecutorLogic().remove(context.getUser(), executorIds);
    }
}
