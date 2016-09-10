package ru.runa.wfe.var.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.commons.SQLCommons;
import ru.runa.wfe.commons.SQLCommons.StringEqualsExpression;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

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

    public void deleteAll(Process process) {
        log.debug("deleting variables for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from Variable where process=?", process);
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

    /**
     * @deprecated Use {@link VariableLoader} in case of mass variable loading.
     */
    @Deprecated
    public Object getVariableValue(ProcessDefinition processDefinition, Process process, VariableDefinition variableDefinition) {
        return new VariableLoader(this, null).getVariableValue(processDefinition, process, variableDefinition);
    }

    /**
     * @deprecated Use {@link VariableLoader} in case of mass variable loading.
     */
    @Deprecated
    public Object processComplexVariablesPre430(ProcessDefinition processDefinition, VariableDefinition variableDefinition, UserType userType,
            Object value) {
        return VariableLoader.processComplexVariablesPre430(processDefinition, variableDefinition, userType, value);
    }

    /**
     * @deprecated Use {@link VariableLoader} in case of mass variable loading.
     */
    @Deprecated
    public WfVariable getVariable(ProcessDefinition processDefinition, Process process, String variableName) {
        return new VariableLoader(this, null).getVariable(processDefinition, process, variableName);
    }
}
