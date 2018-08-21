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
import ru.runa.wfe.execution.BaseNodeProcess;
import ru.runa.wfe.execution.BaseProcess;
import ru.runa.wfe.execution.BaseToken;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

@Component
public class NodeProcessDao2 extends GenericDao2<BaseNodeProcess, NodeProcess, NodeProcessDao, ArchivedNodeProcess, ArchivedNodeProcessDao> {

    @Autowired
    protected NodeProcessDao2(NodeProcessDao dao1, ArchivedNodeProcessDao dao2) {
        super(dao1, dao2);
    }

    /**
     * @return Null if unknown.
     */
    private Boolean calculateIsArchive(BaseProcess process, BaseToken token) {
        if (process != null) {
            Preconditions.checkState(token == null || process.isArchive() == token.isArchive());
            return process.isArchive();
        } else if (token != null) {
            return token.isArchive();
        } else {
            return null;
        }
    }

    // TODO If finished != null, dao.getNodeProcesses() checks Process.endDate;
    //      maybe we can optimize this by checking executionStatus instead, and thus not-querying archive if finished == true?
    public List<? extends BaseNodeProcess> getNodeProcesses(BaseProcess process, BaseToken parentToken, String nodeId, Boolean finished) {
        Boolean isArchive = calculateIsArchive(process, parentToken);
        if (isArchive == null) {
            val result = new ArrayList<BaseNodeProcess>();
            result.addAll(dao1.getNodeProcesses(null, null, nodeId, finished));
            result.addAll(dao2.getNodeProcesses(null, null, nodeId, finished));
            return result;
        } else if (isArchive) {
            return dao2.getNodeProcesses((ArchivedProcess) process, (ArchivedToken) parentToken, nodeId, finished);
        } else {
            return dao1.getNodeProcesses((Process) process, (Token) parentToken, nodeId, finished);
        }
    }

    public List<? extends BaseProcess> getSubprocesses(@NonNull BaseProcess process) {
        if (process.isArchive()) {
            return dao2.getSubprocesses((ArchivedProcess)process);
        } else {
            return dao1.getSubprocesses((Process)process);
        }
    }

    public List<? extends BaseProcess> getSubprocessesRecursive(@NonNull BaseProcess process) {
        if (process.isArchive()) {
            return dao2.getSubprocessesRecursive((ArchivedProcess)process);
        } else {
            return dao1.getSubprocessesRecursive((Process)process);
        }
    }
}
