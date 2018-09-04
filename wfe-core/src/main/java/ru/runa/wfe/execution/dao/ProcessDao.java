package ru.runa.wfe.execution.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.user.Executor;

@Component
public class ProcessDao extends GenericDao2<Process, CurrentProcess, CurrentProcessDao, ArchivedProcess, ArchivedProcessDao> {

    @Autowired
    ProcessDao(CurrentProcessDao dao1, ArchivedProcessDao dao2) {
        super(dao1, dao2);
    }

    @Override
    protected void checkNotNull(Process entity, Long id) {
        if (entity == null) {
            throw new ProcessDoesNotExistException(id);
        }
    }

    /**
     * Returns UNORDERED and POSSIBLY INCOMPLETE list: processes are in NOT the same order as ids;
     * and if some requested process ids don't exist, returned list size will be smaller than ids list size.
     */
    public List<Process> find(List<Long> ids) {
        val result = new ArrayList<Process>(ids.size());
        if (ids.isEmpty()) {
            return result;
        }
        result.addAll(dao1.findImpl(ids));
        result.addAll(dao2.findImpl(ids));
        return result;
    }

    public Set<Long> getDependentProcessIds(Executor executor, int limit) {
        val result = dao1.getDependentProcessIds(executor, limit);
        if (result.size() < limit) {
            result.addAll(dao2.getDependentProcessIds(executor, limit - result.size()));
        }
        return result;
    }

    // TODO Unused.
    public List<Process> getProcesses(final ProcessFilter filter) {
        val result = new ArrayList<Process>();
        result.addAll(dao1.getProcesses(filter));
        result.addAll(dao2.getProcesses(filter));
        return result;
    }
}
