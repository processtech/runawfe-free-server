package ru.runa.wfe.execution.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.ArchivedSwimlane;
import ru.runa.wfe.execution.QArchivedSwimlane;

@Component
public class ArchivedSwimlaneDao extends ReadOnlyGenericDao<ArchivedSwimlane> {

    public ArchivedSwimlaneDao() {
        super(ArchivedSwimlane.class);
    }

    public List<ArchivedSwimlane> findByProcess(ArchivedProcess process) {
        val s = QArchivedSwimlane.archivedSwimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process)).fetch();
    }

    public ArchivedSwimlane findByProcessAndName(ArchivedProcess process, String name) {
        val s = QArchivedSwimlane.archivedSwimlane;
        return queryFactory.selectFrom(s).where(s.process.eq(process).and(s.name.eq(name))).fetchFirst();
    }

    public void deleteAll(ArchivedProcess process) {
        log.debug("deleting swimlanes for archived process " + process.getId());
        val s = QArchivedSwimlane.archivedSwimlane;
        queryFactory.delete(s).where(s.process.eq(process)).execute();
    }
}
