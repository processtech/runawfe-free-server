package ru.runa.wfe.execution.dao;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.ArchivedSwimlane;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;

@Component
@CommonsLog
public class SwimlaneDao extends GenericDao2<Swimlane, CurrentSwimlane, CurrentSwimlaneDao, ArchivedSwimlane, ArchivedSwimlaneDao> {

    @Autowired
    SwimlaneDao(CurrentSwimlaneDao dao1, ArchivedSwimlaneDao dao2) {
        super(dao1, dao2);
    }

    public Swimlane findByProcessAndName(Process process, String name) {
        if (process.isArchive()) {
            return dao2.findByProcessAndName((ArchivedProcess) process, name);
        } else {
            return dao1.findByProcessAndName((CurrentProcess) process, name);
        }
    }
}
