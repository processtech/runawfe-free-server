package ru.runa.wfe.audit.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.ArchivedProcessLog;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.dao.ProcessDao2;

@Component
public class ProcessLogDao2 extends GenericDao2<IProcessLog, ProcessLog, ProcessLogDao, ArchivedProcessLog, ArchivedProcessLogDao> {

    private ProcessDao2 processDao2;

    @Autowired
    protected ProcessLogDao2(ProcessLogDao dao1, ArchivedProcessLogDao dao2, ProcessDao2 processDao2) {
        super(dao1, dao2);
        this.processDao2 = processDao2;
    }

    public List<IProcessLog> getAll(final ProcessLogFilter filter) {
        val process = filter.getProcessId() != null
                ? processDao2.get(filter.getProcessId())
                : null;
        if (process == null) {
            val current = dao1.getAll(filter);
            val archived = dao2.getAll(filter);
            val result = new ArrayList<IProcessLog>(current.size() + archived.size());
            result.addAll(current);
            result.addAll(archived);
            result.sort(new Comparator<IProcessLog>() {
                @Override
                public int compare(IProcessLog o1, IProcessLog o2) {
                    return Long.compare(o1.getId(), o2.getId());
                }
            });
            return result;
        } else if (!process.isArchive()) {
            return dao1.getAll(filter);
        } else if (filter.getTokenId() != null) {
            // Archive does not have TOKEN_ID field.
            return Collections.emptyList();
        } else {
            return dao2.getAll(filter);
        }
    }
}
