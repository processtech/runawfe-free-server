package ru.runa.wfe.execution.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.QCurrentSwimlane;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.AssignmentException;
import ru.runa.wfe.lang.SwimlaneDefinition;

/**
 * DAO for {@link CurrentSwimlane}.
 *
 * @author dofs
 * @since 4.0
 */
@Component
public class CurrentSwimlaneDao extends GenericDao<CurrentSwimlane> {

    public CurrentSwimlaneDao() {
        super(CurrentSwimlane.class);
    }

    public List<CurrentSwimlane> findByProcess(CurrentProcess process) {
        val s = QCurrentSwimlane.currentSwimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process)).fetch();
    }

    public CurrentSwimlane findByProcessAndName(CurrentProcess process, String name) {
        val s = QCurrentSwimlane.currentSwimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process).and(s.name.eq(name))).fetchFirst();
    }

    public CurrentSwimlane findOrCreate(CurrentProcess process, SwimlaneDefinition swimlaneDefinition) {
        CurrentSwimlane swimlane = findByProcessAndName(process, swimlaneDefinition.getName());
        if (swimlane == null) {
            swimlane = new CurrentSwimlane(swimlaneDefinition.getName());
            swimlane.setProcess(process);
            create(swimlane);
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

    public void deleteAll(CurrentProcess process) {
        log.debug("deleting swimlanes for process " + process.getId());
        val s = QCurrentSwimlane.currentSwimlane;
        queryFactory.delete(s).where(s.process.eq(process)).execute();
    }
}
