package ru.runa.wfe.var.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.commons.SQLCommons;
import ru.runa.wfe.commons.SQLCommons.StringEqualsExpression;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.Variable;

import com.google.common.collect.Maps;

import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class VariableDAO extends GenericDAO<Variable> {

    public Variable<?> get(Process process, String name) {
        return findFirstOrNull("from Variable where process=? and name=?", process, name);
    }

    public List<Variable<?>> findByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        StringEqualsExpression expression = SQLCommons.getStringEqualsExpression(variableNamePattern);
        String query = "from Variable where name " + expression.getComparisonOperator() + " ? and stringValue = ?";
        return getHibernateTemplate().find(query, expression.getValue(), stringValue);
    }

    /**
     * @return all variable values.
     */
    public Map<String, Object> getAll(Process process) {
        Map<String, Object> variables = Maps.newHashMap();
        List<Variable<?>> list = getHibernateTemplate().find("from Variable where process=?", process);
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
    public Map<Process, Map<String, Variable<?>>> getVariables(Collection<Process> processes) {
        Map<Process, Map<String, Variable<?>>> result = Maps.newHashMap();
        if (Utils.isNullOrEmpty(processes)) {
            return result;
        }
        for (Process process : processes) {
            result.put(process, Maps.<String, Variable<?>> newHashMap());
        }
        List<Variable<?>> list = getHibernateTemplate().findByNamedParam("from Variable where process in (:processes)", "processes", processes);
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
    public Map<Process, Map<String, Variable<?>>> getVariables(Set<Process> processes, List<String> variableNames) {
        if (Utils.isNullOrEmpty(processes) || Utils.isNullOrEmpty(variableNames)) {
            return null;
        }
        Map<Process, Map<String, Variable<?>>> result = Maps.newHashMap();
        for (Process process : processes) {
            Map<String, Variable<?>> processVariables = Maps.newHashMap();
            result.put(process, processVariables);
            for (String variable : variableNames) {
                processVariables.put(variable, null);
            }
        }
        List<Variable<?>> list = getHibernateTemplate().findByNamedParam("from Variable where process in (:processes) and name in (:variableNames)",
                new String[] { "processes", "variableNames" }, new Object[] { processes, variableNames });
        for (Variable<?> variable : list) {
            result.get(variable.getProcess()).put(variable.getName(), variable);
        }
        return result;
    }

    public void deleteAll(Process process) {
        log.debug("deleting variables for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from Variable where process=?", process);
    }

}
