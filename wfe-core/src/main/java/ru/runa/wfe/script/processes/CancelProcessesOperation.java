package ru.runa.wfe.script.processes;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;

@XmlType(name = CancelProcessesOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class CancelProcessesOperation extends FilterableProcessInstancesOperation {

    public static final String SCRIPT_NAME = "cancelProcesses";

    @Override
    public void execute(ScriptExecutionContext context) {
        context.getExecutionLogic().cancelProcesses(context.getUser(), createProcessFilter());
    }
}
