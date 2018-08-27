package ru.runa.wfe.execution.dao;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedNodeProcess;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.ArchivedToken;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.CurrentNodeProcess;
import ru.runa.wfe.execution.CurrentToken;

@Component
public class NodeProcessDao extends GenericDao2<NodeProcess, CurrentNodeProcess, CurrentNodeProcessDao, ArchivedNodeProcess, ArchivedNodeProcessDao> {

    @Autowired
    protected NodeProcessDao(CurrentNodeProcessDao dao1, ArchivedNodeProcessDao dao2) {
        super(dao1, dao2);
    }

    /**
     * @return Null if unknown.
     */
    private Boolean calculateIsArchive(Process process, Token token) {
        if (process != null) {
            Preconditions.checkState(token == null || process.isArchive() == token.isArchive());
            return process.isArchive();
        } else if (token != null) {
            return token.isArchive();
        } else {
            return null;
        }
    }

    public NodeProcess findBySubProcessId(Long subProcessId) {
        NodeProcess result = dao1.findBySubProcessId(subProcessId);
        if (result == null) {
            result = dao2.findBySubProcessId(subProcessId);
        }
        return result;
    }

    // TODO If finished != null, dao.getNodeProcesses() checks Process.endDate;
    //      maybe we can optimize this by checking executionStatus instead, and thus not-querying archive if finished == true?
    public List<? extends NodeProcess> getNodeProcesses(Process process, Token parentToken, String nodeId, Boolean finished) {
        Boolean isArchive = calculateIsArchive(process, parentToken);
        if (isArchive == null) {
            val result = new ArrayList<NodeProcess>();
            result.addAll(dao1.getNodeProcesses(null, null, nodeId, finished));
            result.addAll(dao2.getNodeProcesses(null, null, nodeId, finished));
            return result;
        } else if (isArchive) {
            return dao2.getNodeProcesses((ArchivedProcess) process, (ArchivedToken) parentToken, nodeId, finished);
        } else {
            return dao1.getNodeProcesses((CurrentProcess) process, (CurrentToken) parentToken, nodeId, finished);
        }
    }

    public List<? extends Process> getSubprocesses(@NonNull Process process) {
        if (process.isArchive()) {
            return dao2.getSubprocesses((ArchivedProcess)process);
        } else {
            return dao1.getSubprocesses((CurrentProcess)process);
        }
    }

    public List<? extends Process> getSubprocessesRecursive(@NonNull Process process) {
        if (process.isArchive()) {
            return dao2.getSubprocessesRecursive((ArchivedProcess)process);
        } else {
            return dao1.getSubprocessesRecursive((CurrentProcess)process);
        }
    }
}
