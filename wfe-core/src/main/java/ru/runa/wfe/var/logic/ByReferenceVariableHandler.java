package ru.runa.wfe.var.logic;

import com.google.common.base.Preconditions;
import ru.runa.wfe.InternalApplicationException;
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

    public WfVariable resolve(WfVariable wfVariable, InternalStorageReferenceService refService) {
        return reader.resolve(wfVariable, refService);
    }

    public WfVariable resolveContainer(WfVariable wfVariable, InternalStorageReferenceService componentRefService) {
        return reader.resolveContainer(wfVariable, componentRefService);
    }

    public ByReferenceWriteResult write(VariableDefinition variableDefinition, Object value,
                                        InternalStorageReferenceService refService) {
        return writer.write(variableDefinition, value, refService);
    }

    public ByReferenceWriteResult writeContainer(VariableDefinition variableDefinition, Object value,
                                                 InternalStorageReferenceService componentRefService) {
        return writer.writeContainer(variableDefinition, value, componentRefService);
    }

    public ByReferenceWriteResult tryWrite(VariableDefinition variableDefinition, Object value,
                                           InternalStorageReferenceServiceRouter router) {
        if (variableDefinition.isUserType() && variableDefinition.getUserType().isByReference()) {
            InternalStorageReferenceService refService = router.forUserType(variableDefinition.getUserType());
            return write(variableDefinition, value, refService);
        }
        if (isContainerOfByReference(variableDefinition)) {
            InternalStorageReferenceService componentRefService = resolveContainerComponentService(variableDefinition, router);
            return writeContainer(variableDefinition, value, componentRefService);
        }
        return null;
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

    public static InternalStorageReferenceService resolveContainerComponentService(VariableDefinition variableDefinition,
                                                                                   InternalStorageReferenceServiceRouter router) {
        Preconditions.checkState(isContainerOfByReference(variableDefinition),
                "resolveContainerComponentService called for non by-reference container '%s'", variableDefinition.getName());
        for (UserType ut : variableDefinition.getFormatComponentUserTypes()) {
            if (ut != null && ut.isByReference()) {
                return router.forUserType(ut);
            }
        }
        throw new InternalApplicationException(
                "isContainerOfByReference() returned true but no by-reference component user type found in '"
                        + variableDefinition.getName() + "'");
    }
}
