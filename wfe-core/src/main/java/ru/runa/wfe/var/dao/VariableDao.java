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
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.var.QVariable;
import ru.runa.wfe.var.Variable;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class VariableDao extends GenericDao<Variable> {

    public Variable<?> get(CurrentProcess process, String name) {
        val v = QVariable.variable;
        return queryFactory.selectFrom(v).where(v.process.eq(process).and(v.name.eq(name))).fetchFirst();
    }

    public List<Variable<?>> findByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        StringEqualsExpression expression = SqlCommons.getStringEqualsExpression(variableNamePattern);
        return sessionFactory.getCurrentSession().createQuery("from Variable where name " + expression.getComparisonOperator() + " :name and stringValue = :value")
                .setParameter("name", expression.getValue())
                .setParameter("value", stringValue)
                .list();
    }

    /**
     * @return all variable values.
     */
    public Map<String, Object> getAll(CurrentProcess process) {
        Map<String, Object> variables = Maps.newHashMap();
        val v = QVariable.variable;
        List<Variable<?>> list = queryFactory.selectFrom(v).where(v.process.eq(process)).fetch();
        for (Variable<?> variable : list) {
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
    public Map<CurrentProcess, Map<String, Variable<?>>> getVariables(Collection<CurrentProcess> processes) {
        Map<CurrentProcess, Map<String, Variable<?>>> result = Maps.newHashMap();
        if (Utils.isNullOrEmpty(processes)) {
            return result;
        }
        for (CurrentProcess process : processes) {
            result.put(process, Maps.newHashMap());
        }
        val v = QVariable.variable;
        List<Variable<?>> list = queryFactory.selectFrom(v).where(v.process.in(processes)).fetch();
        for (Variable<?> variable : list) {
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
    public Map<CurrentProcess, Map<String, Variable<?>>> getVariables(Set<CurrentProcess> processes, List<String> variableNames) {
        if (Utils.isNullOrEmpty(processes) || Utils.isNullOrEmpty(variableNames)) {
            return null;
        }
        Map<CurrentProcess, Map<String, Variable<?>>> result = Maps.newHashMap();
        for (CurrentProcess process : processes) {
            Map<String, Variable<?>> processVariables = Maps.newHashMap();
            result.put(process, processVariables);
            for (String variable : variableNames) {
                processVariables.put(variable, null);
            }
        }
        List<Variable<?>> list = new ArrayList<>();
        for (List<CurrentProcess> processesPart : Lists.partition(Lists.newArrayList(processes), SystemProperties.getDatabaseParametersCount())) {
            val v = QVariable.variable;
            list.addAll(queryFactory.selectFrom(v).where(v.process.in(processesPart).and(v.name.in(variableNames))).fetch());
        }
        for (Variable<?> variable : list) {
            result.get(variable.getProcess()).put(variable.getName(), variable);
        }
        return result;
    }

    public void deleteAll(CurrentProcess process) {
        log.debug("deleting variables for process " + process.getId());
        val v = QVariable.variable;
        queryFactory.delete(v).where(v.process.eq(process)).execute();
    }
}
