package ru.runa.wfe.execution.dao;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ArchiveAwareGenericDao;
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
public class NodeProcessDao extends ArchiveAwareGenericDao<NodeProcess, CurrentNodeProcess, CurrentNodeProcessDao, ArchivedNodeProcess, ArchivedNodeProcessDao> {

    @Autowired
    protected NodeProcessDao(CurrentNodeProcessDao currentDao, ArchivedNodeProcessDao archivedDao) {
        super(currentDao, archivedDao);
    }

    /**
     * @return Null if unknown.
     */
    private Boolean calculateIsArchive(Process process, Token token) {
        if (process != null) {
            Preconditions.checkState(token == null || process.isArchived() == token.isArchived());
            return process.isArchived();
        } else if (token != null) {
            return token.isArchived();
        } else {
            return null;
        }
    }

    public NodeProcess findBySubProcessId(Long subProcessId) {
        NodeProcess result = currentDao.findBySubProcessId(subProcessId);
        if (result == null) {
            result = archivedDao.findBySubProcessId(subProcessId);
        }
        return result;
    }

    public NodeProcess findBySubProcess(Process subProcess) {
        if (subProcess.isArchived()) {
            return archivedDao.findBySubProcessId(subProcess.getId());
        } else {
            return currentDao.findBySubProcessId(subProcess.getId());
        }
    }

    // TODO If finished != null, dao.getNodeProcesses() checks Process.endDate;
    //      maybe we can optimize this by checking executionStatus instead, and thus not-querying archive if finished == true?
    public List<? extends NodeProcess> getNodeProcesses(Process process, Token parentToken, String nodeId, Boolean finished) {
        Boolean isArchive = calculateIsArchive(process, parentToken);
        if (isArchive == null) {
            val result = new ArrayList<NodeProcess>();
            result.addAll(currentDao.getNodeProcesses(null, null, nodeId, finished));
            result.addAll(archivedDao.getNodeProcesses(null, null, nodeId, finished));
            return result;
        } else if (isArchive) {
            return archivedDao.getNodeProcesses((ArchivedProcess) process, (ArchivedToken) parentToken, nodeId, finished);
        } else {
            return currentDao.getNodeProcesses((CurrentProcess) process, (CurrentToken) parentToken, nodeId, finished);
        }
    }

    public List<? extends Process> getSubprocesses(@NonNull Process process) {
        if (process.isArchived()) {
            return archivedDao.getSubprocesses((ArchivedProcess) process);
        } else {
            return currentDao.getSubprocesses((CurrentProcess) process);
        }
    }

    public List<? extends Process> getSubprocesses(@NonNull Token token) {
        if (token.isArchived()) {
            return archivedDao.getSubprocesses((ArchivedToken) token);
        } else {
            return currentDao.getSubprocesses((CurrentToken) token);
        }
    }

    public List<? extends Process> getSubprocessesRecursive(@NonNull Process process) {
        if (process.isArchived()) {
            return archivedDao.getSubprocessesRecursive((ArchivedProcess) process);
        } else {
            return currentDao.getSubprocessesRecursive((CurrentProcess) process);
        }
    }
}
