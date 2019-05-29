package ru.runa.wfe.var.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.QCurrentVariable;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CurrentVariableDao extends GenericDao<CurrentVariable> {

    public CurrentVariableDao() {
        super(CurrentVariable.class);
    }

    public CurrentVariable<?> get(CurrentProcess process, String name) {
        val v = QCurrentVariable.currentVariable;
        return queryFactory.selectFrom(v).where(v.process.eq(process).and(v.name.eq(name))).fetchFirst();
    }

    List<CurrentVariable<?>> getAllImpl(CurrentProcess process) {
        val v = QCurrentVariable.currentVariable;
        return queryFactory.selectFrom(v).where(v.process.eq(process)).fetch();
    }

    List<CurrentVariable<?>> getVariablesImpl(List<CurrentProcess> processesPart, List<String> variableNamesOrNull) {
        val v = QCurrentVariable.currentVariable;
        val q = queryFactory.selectFrom(v).where(v.process.in(processesPart));
        if (variableNamesOrNull != null) {
            q.where(v.name.in(variableNamesOrNull));
        }
        return q.fetch();
    }

    public void deleteAll(CurrentProcess process) {
        log.debug("deleting variables for process " + process.getId());
        val v = QCurrentVariable.currentVariable;
        queryFactory.delete(v).where(v.process.eq(process)).execute();
    }

    /**
     * Used by TNMS.
     */
    @SuppressWarnings({"unused", "unchecked"})
    public List<CurrentVariable> findNonEndedByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        SqlCommons.StringEqualsExpression expression = SqlCommons.getStringEqualsExpression(variableNamePattern);

        return sessionFactory.getCurrentSession()
                .createQuery("from CurrentVariable " +
                        "where process.executionStatus != 'ENDED' " +
                        "  and name " + expression.getComparisonOperator() + " :name " +
                        "  and stringValue = :value")
                .setParameter("name", expression.getValue())
                .setParameter("value", stringValue)
                .list();
    }
}
