package ru.runa.wfe.execution.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.QCurrentSwimlane;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.AssignmentException;
import ru.runa.wfe.extension.assign.NoExecutorAssignedException;
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

    public List<CurrentSwimlane> findByNamePatternInActiveProcesses(String name) {
        val s = QCurrentSwimlane.currentSwimlane;
        return queryFactory.selectFrom(s).where(s.name.like(name).and(s.process.executionStatus.notIn(ExecutionStatus.ENDED))).fetch();
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
                if (swimlane.getExecutor() != null) {
                    ApplicationContextFactory.getExecutionLogic().removeTokenError(executionContext.getCurrentToken());
                } else {
                    ApplicationContextFactory.getExecutionLogic().failToken(executionContext.getCurrentToken(), new NoExecutorAssignedException());
                }
            } catch (Exception e) {
                if (ApplicationContextFactory.getExecutionLogic().failToken(executionContext.getCurrentToken(), e)) {
                    if (e instanceof AssignmentException) {
                        log.warn("Unable to assign in " + swimlane + " due to " + e);
                    } else {
                        log.warn("Unable to assign in " + swimlane, e);
                    }
                    swimlane.assignExecutor(executionContext, null, true);
                }
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
