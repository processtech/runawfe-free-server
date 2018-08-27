package ru.runa.wfe.execution.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.QCurrentSwimlane;
import ru.runa.wfe.execution.QSwimlane;
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

    public List<CurrentSwimlane> findByProcess(CurrentProcess process) {
        val s = QCurrentSwimlane.currentSwimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process)).fetch();
    }

    public CurrentSwimlane findByProcessAndName(CurrentProcess process, String name) {
        val s = QCurrentSwimlane.currentSwimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process).and(s.name.eq(name))).fetchFirst();
    }

    public void deleteAll(CurrentProcess process) {
        log.debug("deleting swimlanes for process " + process.getId());
        val s = QCurrentSwimlane.currentSwimlane;
        queryFactory.delete(s).where(s.process.eq(process)).execute();
    }
}
