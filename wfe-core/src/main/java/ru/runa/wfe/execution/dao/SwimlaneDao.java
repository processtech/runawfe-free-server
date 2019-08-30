package ru.runa.wfe.execution.dao;

import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.QSwimlane;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.AssignmentException;
import ru.runa.wfe.extension.assign.NoExecutorAssignedException;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.SwimlaneDefinition;

/**
 * DAO for {@link Swimlane}.
 *
 * @author dofs
 * @since 4.0
 */
@Component
public class SwimlaneDao extends GenericDao<Swimlane> {

    public List<Swimlane> findByProcess(Process process) {
        QSwimlane s = QSwimlane.swimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process)).fetch();
    }

    public Swimlane findByProcessAndName(Process process, String name) {
        QSwimlane s = QSwimlane.swimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process).and(s.name.eq(name))).fetchFirst();
    }
    
    public List<Swimlane> findByNamePatternInActiveProcesses(String name) {
        QSwimlane s = QSwimlane.swimlane;
        return queryFactory.selectFrom(s).where(s.name.like(name).and(s.process.executionStatus.notIn(ExecutionStatus.ENDED))).fetch();
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
            Node node = executionContext.getNode();
            ProcessError processError = new ProcessError(ProcessErrorType.assignment, executionContext.getProcess().getId(), node.getNodeId());
            try {
                AssignmentHandler assignmentHandler = swimlaneDefinition.getDelegation().getInstance();
                assignmentHandler.assign(executionContext, swimlane);
                if (swimlane.getExecutor() != null) {
                    Errors.removeProcessError(processError);
                } else {
                    Errors.addProcessError(processError, node.getName(), new NoExecutorAssignedException());
                }
            } catch (Exception e) {
                if (Errors.addProcessError(processError, node.getName(), e)) {
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

    public void deleteAll(Process process) {
        log.debug("deleting swimlanes for process " + process.getId());
        QSwimlane s = QSwimlane.swimlane;
        queryFactory.delete(s).where(s.process.eq(process)).execute();
    }
}
