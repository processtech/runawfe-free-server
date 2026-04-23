package ru.runa.wfe.var.logic;

import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dto.WfVariable;

public class ByReferenceVariableHandler {

    private final ByReferenceReader reader;
    private final ByReferenceWriter writer;

    public ByReferenceVariableHandler(
            VariableLoader variableLoader,
            Process process,
            ProcessLogDao processLogDao,
            CurrentProcess currentProcess,
            CurrentToken currentToken
    ) {
        ByReferenceLogger logger = new ByReferenceLogger(processLogDao, currentProcess, currentToken);
        this.reader = new ByReferenceReader(variableLoader, process);
        this.writer = new ByReferenceWriter(variableLoader, process, logger);
    }

    public WfVariable resolve(WfVariable wfVariable) {
        return reader.resolve(wfVariable);
    }

    public WfVariable resolveContainer(WfVariable wfVariable) {
        return reader.resolveContainer(wfVariable);
    }

    public ByReferenceWriteResult write(VariableDefinition variableDefinition, Object value) {
        return writer.write(variableDefinition, value);
    }

    public ByReferenceWriteResult writeContainer(VariableDefinition variableDefinition, Object value) {
        return writer.writeContainer(variableDefinition, value);
    }

    public static boolean isContainerOfByReference(VariableDefinition variableDefinition) {
        UserType[] componentUserTypes = variableDefinition.getFormatComponentUserTypes();
        if (componentUserTypes == null || componentUserTypes.length == 0) {
            return false;
        }
        for (UserType componentUserType : componentUserTypes) {
            if (componentUserType != null && componentUserType.isByReference()) {
                return true;
            }
        }
        return false;
    }
}
