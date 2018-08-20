package ru.runa.wfe.execution.dao;

import com.google.common.collect.Lists;
import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.QNodeProcess;
import ru.runa.wfe.execution.Token;

@Component
public class NodeProcessDao extends GenericDao<NodeProcess> {

    public NodeProcess findBySubProcessId(Long processId) {
        QNodeProcess np = QNodeProcess.nodeProcess;
        return queryFactory.selectFrom(np).where(np.subProcess.id.eq(processId)).fetchFirst();
    }

    public List<NodeProcess> getNodeProcesses(final Process process, final Token parentToken, final String nodeId, final Boolean finished) {
        QNodeProcess np = QNodeProcess.nodeProcess;
        JPQLQuery<NodeProcess> q = queryFactory.selectFrom(np).orderBy(np.id.asc());
        if (process != null) {
            q.where(np.process.eq(process));
        }
        if (parentToken != null) {
            q.where(np.parentToken.eq(parentToken));
        }
        if (nodeId != null) {
            q.where(np.nodeId.eq(nodeId));
        }
        if (finished != null) {
            q.where(finished ? np.subProcess.endDate.isNotNull() : np.subProcess.endDate.isNull());
        }
        return q.fetch();
    }

    public void deleteByProcess(Process process) {
        log.debug("deleting subprocess nodes for process " + process.getId());
        QNodeProcess np = QNodeProcess.nodeProcess;
        queryFactory.delete(np).where(np.process.eq(process)).execute();
    }

    public List<Process> getSubprocesses(Process process) {
        List<NodeProcess> nodeProcesses = getNodeProcesses(process, null, null, null);
        List<Process> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NodeProcess nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    public List<Process> getSubprocessesRecursive(Process process) {
        List<Process> result = Lists.newArrayList();
        for (Process subprocess : getSubprocesses(process)) {
            result.add(subprocess);
            result.addAll(getSubprocessesRecursive(subprocess));
        }
        return result;
    }

    public List<Process> getSubprocesses(Token token) {
        List<NodeProcess> nodeProcesses = getNodeProcesses(token.getProcess(), token, null, null);
        List<Process> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NodeProcess nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    public List<Process> getSubprocesses(Process process, String nodeId, Token parentToken, Boolean finished) {
        List<NodeProcess> nodeProcesses = getNodeProcesses(process, parentToken, nodeId, finished);
        List<Process> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NodeProcess nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }
}
