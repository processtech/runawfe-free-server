package ru.runa.wfe.execution.dao;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ArchiveAwareGenericDao;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.ArchivedSwimlane;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;

@Component
public class SwimlaneDao extends ArchiveAwareGenericDao<Swimlane, CurrentSwimlane, CurrentSwimlaneDao, ArchivedSwimlane, ArchivedSwimlaneDao> {

    @Autowired
    SwimlaneDao(CurrentSwimlaneDao currentDao, ArchivedSwimlaneDao archivedDao) {
        super(currentDao, archivedDao);
    }

    public List<? extends Swimlane> findByProcess(Process process) {
        if (process.isArchived()) {
            return archivedDao.findByProcess((ArchivedProcess) process);
        } else {
            return currentDao.findByProcess((CurrentProcess) process);
        }
    }

    public Swimlane findByProcessAndName(Process process, String name) {
        if (process.isArchived()) {
            return archivedDao.findByProcessAndName((ArchivedProcess) process, name);
        } else {
            return currentDao.findByProcessAndName((CurrentProcess) process, name);
        }
    }
}
