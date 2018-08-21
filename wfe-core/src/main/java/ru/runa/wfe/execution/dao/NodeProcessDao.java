package ru.runa.wfe.execution.dao;

import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.QNodeProcess;
import ru.runa.wfe.execution.Token;

@Component
public class NodeProcessDao extends BaseNodeProcessDao<Process, Token, NodeProcess> {

    public NodeProcess findBySubProcessId(Long subProcessId) {
        QNodeProcess np = QNodeProcess.nodeProcess;
        return queryFactory.selectFrom(np).where(np.subProcess.id.eq(subProcessId)).fetchFirst();
    }

    @Override
    public List<NodeProcess> getNodeProcesses(Process process, Token parentToken, String nodeId, Boolean finished) {
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
}
