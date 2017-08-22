package ru.runa.wfe.execution.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.lang.SwimlaneDefinition;

/**
 * DAO for {@link Swimlane}.
 *
 * @author dofs
 * @since 4.0
 */
public class SwimlaneDAO extends GenericDAO<Swimlane> {

    public List<Swimlane> findByProcess(Process process) {
        return getHibernateTemplate().find("from Swimlane where process=?", process);
    }

    public Swimlane findByProcessAndName(Process process, String name) {
        return findFirstOrNull("from Swimlane where process=? and name=?", process, name);
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
            AssignmentHandler assignmentHandler = swimlaneDefinition.getDelegation().getInstance();
            assignmentHandler.assign(executionContext, swimlane);
        }
        return swimlane;
    }

    public void deleteAll(Process process) {
        log.debug("deleting swimlanes for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from Swimlane where process=?", process);
    }
}
