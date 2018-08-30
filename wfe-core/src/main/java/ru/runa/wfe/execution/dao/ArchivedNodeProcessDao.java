package ru.runa.wfe.execution.dao;

import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.execution.ArchivedNodeProcess;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.ArchivedToken;
import ru.runa.wfe.execution.QArchivedNodeProcess;

@Component
public class ArchivedNodeProcessDao
        extends ReadOnlyGenericDao<ArchivedNodeProcess>
        implements BaseNodeProcessDao<ArchivedProcess, ArchivedToken, ArchivedNodeProcess>
{
    public ArchivedNodeProcessDao() {
        super(ArchivedNodeProcess.class);
    }

    public ArchivedNodeProcess findBySubProcessId(Long subProcessId) {
        val np = QArchivedNodeProcess.archivedNodeProcess;
        return queryFactory.selectFrom(np).where(np.subProcess.id.eq(subProcessId)).fetchFirst();
    }

    @Override
    public List<ArchivedNodeProcess> getNodeProcesses(ArchivedProcess process, ArchivedToken parentToken, String nodeId, Boolean finished) {
        val np = QArchivedNodeProcess.archivedNodeProcess;
        JPQLQuery<ArchivedNodeProcess> q = queryFactory.selectFrom(np).orderBy(np.id.asc());
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
}
