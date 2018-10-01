package ru.runa.wfe.execution.dao;

import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentNodeProcess;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.QCurrentNodeProcess;
import ru.runa.wfe.execution.QCurrentProcess;

@Component
public class CurrentNodeProcessDao
        extends GenericDao<CurrentNodeProcess>
        implements BaseNodeProcessDao<CurrentToken, CurrentProcess, CurrentNodeProcess>
{
    private CommonNodeProcessDao<CurrentToken, CurrentProcess, CurrentNodeProcess> delegate;

    public CurrentNodeProcessDao() {
        super(CurrentNodeProcess.class);
        delegate = new CommonNodeProcessDao<>(this);
    }

    public CurrentNodeProcess findBySubProcessId(Long subProcessId) {
        val np = QCurrentNodeProcess.currentNodeProcess;
        return queryFactory.selectFrom(np).where(np.subProcess.id.eq(subProcessId)).fetchFirst();
    }

    public CurrentProcess getRootProcessByParentProcess(CurrentProcess parentProcess) {
        val p = QCurrentProcess.currentProcess;
        val np = QCurrentNodeProcess.currentNodeProcess;
        val result = queryFactory.select(p).from(np).innerJoin(np.rootProcess, p).where(np.process.id.eq(parentProcess.getId())).fetchFirst();
        return result != null ? result : parentProcess;
    }

    @Override
    public List<CurrentNodeProcess> getNodeProcesses(CurrentProcess process, CurrentToken parentToken, String nodeId, Boolean finished) {
        val np = QCurrentNodeProcess.currentNodeProcess;
        JPQLQuery<CurrentNodeProcess> q = queryFactory.selectFrom(np).orderBy(np.id.asc());
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

    @Override
    public List<CurrentProcess> getSubprocesses(CurrentProcess process) {
        return delegate.getSubprocesses(process);
    }

    @Override
    public List<CurrentProcess> getSubprocessesRecursive(CurrentProcess process) {
        return delegate.getSubprocessesRecursive(process);
    }

    @Override
    public List<CurrentProcess> getSubprocesses(CurrentToken token) {
        return delegate.getSubprocesses(token);
    }

    @Override
    public List<CurrentProcess> getSubprocesses(CurrentProcess process, String nodeId, CurrentToken parentToken, Boolean finished) {
        return delegate.getSubprocesses(process, nodeId, parentToken, finished);
    }

    public void deleteByProcess(CurrentProcess process) {
        log.debug("deleting subprocess nodes for process " + process.getId());
        val np = QCurrentNodeProcess.currentNodeProcess;
        queryFactory.delete(np).where(np.process.eq(process)).execute();
    }
}
