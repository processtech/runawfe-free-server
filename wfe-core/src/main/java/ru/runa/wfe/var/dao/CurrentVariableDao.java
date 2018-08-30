package ru.runa.wfe.var.dao;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.Utils;
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

    /**
     * Load all variables for given processes.
     *
     * @param processes
     *            Processes, which variables must be loaded.
     * @return for each given process: map from variable name to loaded variable.
     */
    public Map<CurrentProcess, Map<String, CurrentVariable<?>>> getVariables(Collection<CurrentProcess> processes) {
        Map<CurrentProcess, Map<String, CurrentVariable<?>>> result = Maps.newHashMap();
        if (Utils.isNullOrEmpty(processes)) {
            return result;
        }
        for (CurrentProcess process : processes) {
            result.put(process, Maps.newHashMap());
        }
        val v = QCurrentVariable.currentVariable;
        List<CurrentVariable<?>> list = queryFactory.selectFrom(v).where(v.process.in(processes)).fetch();
        for (CurrentVariable<?> variable : list) {
            result.get(variable.getProcess()).put(variable.getName(), variable);
        }
        return result;
    }

    List<CurrentVariable<?>> getVariablesImpl(List<CurrentProcess> processesPart, List<String> variableNames) {
        val v = QCurrentVariable.currentVariable;
        return queryFactory.selectFrom(v).where(v.process.in(processesPart).and(v.name.in(variableNames))).fetch();
    }

    public void deleteAll(CurrentProcess process) {
        log.debug("deleting variables for process " + process.getId());
        val v = QCurrentVariable.currentVariable;
        queryFactory.delete(v).where(v.process.eq(process)).execute();
    }
}
