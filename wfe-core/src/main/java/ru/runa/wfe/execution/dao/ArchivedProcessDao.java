package ru.runa.wfe.execution.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.QArchivedProcess;

@Component
public class ArchivedProcessDao extends ReadOnlyGenericDao<ArchivedProcess> {

    public ArchivedProcessDao() {
        super(ArchivedProcess.class);
    }

    List<ArchivedProcess> findImpl(List<Long> ids) {
        val p = QArchivedProcess.archivedProcess;
        return queryFactory.selectFrom(p).where(p.id.in(ids)).fetch();
    }
}
