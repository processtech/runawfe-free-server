package ru.runa.wfe.var.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.SqlCommons.StringEqualsExpression;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.QCurrentVariable;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CurrentVariableDao extends BaseVariableDao<CurrentVariable> {

    public CurrentVariableDao() {
        super(CurrentVariable.class);
    }

    public CurrentVariable<?> get(CurrentProcess process, String name) {
        val v = QCurrentVariable.currentVariable;
        return queryFactory.selectFrom(v).where(v.process.eq(process).and(v.name.eq(name))).fetchFirst();
    }

    public List<CurrentVariable<?>> findByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        StringEqualsExpression expression = SqlCommons.getStringEqualsExpression(variableNamePattern);
        return sessionFactory.getCurrentSession().createQuery("from CurrentVariable where name " + expression.getComparisonOperator() + " :name and stringValue = :value")
                .setParameter("name", expression.getValue())
                .setParameter("value", stringValue)
                .list();
    }

    /**
     * @return all variable values.
     */
    public Map<String, Object> getAll(CurrentProcess process) {
        Map<String, Object> variables = Maps.newHashMap();
        val v = QCurrentVariable.currentVariable;
        List<CurrentVariable<?>> list = queryFactory.selectFrom(v).where(v.process.eq(process)).fetch();
        for (CurrentVariable<?> variable : list) {
            try {
                variables.put(variable.getName(), variable.getValue());
            } catch (Exception e) {
                log.error("Unable to revert " + variable + " in " + process, e);
            }
        }
        return variables;
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

    /**
     * Load variables with given names for given processes.
     *
     * @param processes
     *            Processes, which variables must be loaded.
     * @param variableNames
     *            Variable names, which must be loaded for processes.
     * @return for each given process: map from variable name to loaded variable. If no variable loaded for some variable name+process, when map is
     *         still contains variable name as key, but it value is null (Result is filled completely for all processes and variable names).
     */
    public Map<CurrentProcess, Map<String, CurrentVariable<?>>> getVariables(Set<CurrentProcess> processes, List<String> variableNames) {
        if (Utils.isNullOrEmpty(processes) || Utils.isNullOrEmpty(variableNames)) {
            return null;
        }
        Map<CurrentProcess, Map<String, CurrentVariable<?>>> result = Maps.newHashMap();
        for (CurrentProcess process : processes) {
            Map<String, CurrentVariable<?>> processVariables = Maps.newHashMap();
            result.put(process, processVariables);
            for (String variable : variableNames) {
                processVariables.put(variable, null);
            }
        }
        List<CurrentVariable<?>> list = new ArrayList<>();
        for (List<CurrentProcess> processesPart : Lists.partition(Lists.newArrayList(processes), SystemProperties.getDatabaseParametersCount())) {
            val v = QCurrentVariable.currentVariable;
            list.addAll(queryFactory.selectFrom(v).where(v.process.in(processesPart).and(v.name.in(variableNames))).fetch());
        }
        for (CurrentVariable<?> variable : list) {
            result.get(variable.getProcess()).put(variable.getName(), variable);
        }
        return result;
    }

    public void deleteAll(CurrentProcess process) {
        log.debug("deleting variables for process " + process.getId());
        val v = QCurrentVariable.currentVariable;
        queryFactory.delete(v).where(v.process.eq(process)).execute();
    }
}
