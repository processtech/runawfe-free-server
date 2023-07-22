package ru.runa.wfe.script.processes;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;

@XmlType(name = RemoveProcessesOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemoveProcessesOperation extends FilterableProcessInstancesOperation {

    public static final String SCRIPT_NAME = "removeProcesses";

    @Override
    public void execute(ScriptExecutionContext context) {
        context.getExecutionLogic().deleteProcesses(context.getUser(), createProcessFilter());
    }
}
