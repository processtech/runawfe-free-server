package ru.runa.wfe.execution.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.ProcessDoesNotExistException;

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
}
