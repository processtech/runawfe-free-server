package ru.runa.wfe.execution.dao;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.ArchivedSwimlane;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.AssignmentException;
import ru.runa.wfe.lang.SwimlaneDefinition;

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

    public CurrentSwimlane findOrCreate(CurrentProcess process, SwimlaneDefinition swimlaneDefinition) {
        CurrentSwimlane swimlane = dao1.findByProcessAndName(process, swimlaneDefinition.getName());
        if (swimlane == null) {
            swimlane = new CurrentSwimlane(swimlaneDefinition.getName());
            swimlane.setProcess(process);
            dao1.create(swimlane);
        }
        return swimlane;
    }

    public CurrentSwimlane findOrCreateInitialized(ExecutionContext executionContext, SwimlaneDefinition swimlaneDefinition, boolean reassign) {
        CurrentSwimlane swimlane = findOrCreate(executionContext.getCurrentProcess(), swimlaneDefinition);
        if (reassign || swimlane.getExecutor() == null) {
            try {
                AssignmentHandler assignmentHandler = swimlaneDefinition.getDelegation().getInstance();
                assignmentHandler.assign(executionContext, swimlane);
            } catch (AssignmentException e) {
                log.warn("Unable to assign in " + swimlane + " due to " + e);
            } catch (Exception e) {
                log.warn("Unable to assign in " + swimlane, e);
            }
        }
        return swimlane;
    }
}
