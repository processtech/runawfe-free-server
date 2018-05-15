package ru.runa.wfe.execution.dao;

import java.util.List;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.QSwimlane;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.AssignmentException;
import ru.runa.wfe.lang.SwimlaneDefinition;

/**
 * DAO for {@link Swimlane}.
 *
 * @author dofs
 * @since 4.0
 */
public class SwimlaneDAO extends GenericDAO<Swimlane> {

    public List<Swimlane> findByProcess(Process process) {
        QSwimlane s = QSwimlane.swimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process)).fetch();
    }

    public Swimlane findByProcessAndName(Process process, String name) {
        QSwimlane s = QSwimlane.swimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process).and(s.name.eq(name))).fetchFirst();
    }

    public Swimlane findOrCreate(Process process, SwimlaneDefinition swimlaneDefinition) {
        Swimlane swimlane = findByProcessAndName(process, swimlaneDefinition.getName());
        if (swimlane == null) {
            swimlane = new Swimlane(swimlaneDefinition.getName());
            swimlane.setProcess(process);
            create(swimlane);
        }
        return swimlane;
    }

    public Swimlane findOrCreateInitialized(ExecutionContext executionContext, SwimlaneDefinition swimlaneDefinition, boolean reassign) {
        Swimlane swimlane = findOrCreate(executionContext.getProcess(), swimlaneDefinition);
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

    public void deleteAll(Process process) {
        log.debug("deleting swimlanes for process " + process.getId());
        QSwimlane s = QSwimlane.swimlane;
        queryFactory.delete(s).where(s.process.eq(process)).execute();
    }
}
