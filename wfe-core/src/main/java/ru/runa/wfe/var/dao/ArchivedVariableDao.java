package ru.runa.wfe.var.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.var.ArchivedVariable;
import ru.runa.wfe.var.QArchivedVariable;

@Component
public class ArchivedVariableDao extends ReadOnlyGenericDao<ArchivedVariable> {

    public ArchivedVariableDao() {
        super(ArchivedVariable.class);
    }

    public ArchivedVariable<?> get(ArchivedProcess process, String name) {
        val v = QArchivedVariable.archivedVariable;
        return queryFactory.selectFrom(v).where(v.process.eq(process).and(v.name.eq(name))).fetchFirst();
    }

    List<ArchivedVariable<?>> getAllImpl(ArchivedProcess process) {
        val v = QArchivedVariable.archivedVariable;
        return queryFactory.selectFrom(v).where(v.process.eq(process)).fetch();
    }

    List<ArchivedVariable<?>> getVariablesImpl(List<ArchivedProcess> processesPart, List<String> variableNamesOrNull) {
        val v = QArchivedVariable.archivedVariable;
        val q = queryFactory.selectFrom(v).where(v.process.in(processesPart));
        if (variableNamesOrNull != null) {
            q.where(v.name.in(variableNamesOrNull));
        }
        return q.fetch();
    }

    public List<ArchivedVariable<?>> getVariablesByNameStartsWith(ArchivedProcess process, String namePrefix) {
        final QArchivedVariable variable = QArchivedVariable.archivedVariable;
        return queryFactory.selectFrom(variable).where(variable.process.eq(process).and(variable.name.startsWith(namePrefix))).fetch();
    }

    public void deleteAll(ArchivedProcess process) {
        log.debug("deleting variables for archived process " + process.getId());
        val v = QArchivedVariable.archivedVariable;
        queryFactory.delete(v).where(v.process.eq(process)).execute();
    }

}
