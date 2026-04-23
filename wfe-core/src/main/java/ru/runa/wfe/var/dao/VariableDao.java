package ru.runa.wfe.var.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dao.ArchiveAwareGenericDao;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.ArchivedVariable;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.Variable;

@Component
@CommonsLog
public class VariableDao extends ArchiveAwareGenericDao<Variable, CurrentVariable, CurrentVariableDao, ArchivedVariable, ArchivedVariableDao> {

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    VariableDao(CurrentVariableDao currentDao, ArchivedVariableDao archivedDao) {
        super(currentDao, archivedDao);
    }

    public Variable get(Process process, String name) {
        if (process.isArchived()) {
            return archivedDao.get((ArchivedProcess) process, name);
        } else {
            return currentDao.get((CurrentProcess) process, name);
        }
    }

    /**
     * @return All variable values.
     */
    public Map<String, Object> getAll(Process process) {
        Map<String, Object> result = Maps.newHashMap();
        List<? extends Variable> vars = process.isArchived()
                ? archivedDao.getAllImpl((ArchivedProcess) process)
                : currentDao.getAllImpl((CurrentProcess) process);
        for (Variable v : vars) {
            try {
                result.put(v.getName(), v.getValue());
            } catch (Exception e) {
                log.error("Unable to revert " + v + " in " + process, e);
            }
        }
        return result;
    }

    /**
     * Load all variables for given processes.
     *
     * @param processes
     *            Processes, which variables must be loaded.
     * @return for each given process: map from variable name to loaded variable.
     */
    public Map<Process, Map<String, Variable>> getVariables(List<? extends Process> processes) {
        return Utils.isNullOrEmpty(processes)
                ? new HashMap<>()
                : getVariablesImpl(processes, null);
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
    public Map<Process, Map<String, Variable>> getVariables(List<? extends Process> processes, List<String> variableNames) {
        return Utils.isNullOrEmpty(processes) || Utils.isNullOrEmpty(variableNames)
                ? null
                : getVariablesImpl(processes, variableNames);
    }

    private Map<Process, Map<String, Variable>> getVariablesImpl(List<? extends Process> processes, List<String> variableNamesOrNull) {
        val result = new HashMap<Process, Map<String, Variable>>();
        val currentProcesses = new ArrayList<CurrentProcess>(processes.size());
        val archivedProcesses = new ArrayList<ArchivedProcess>(processes.size());
        for (Process p : processes) {
            if (p.isArchived()) {
                archivedProcesses.add((ArchivedProcess) p);
            } else {
                currentProcesses.add((CurrentProcess) p);
            }
            val vars = new HashMap<String, Variable>();
            result.put(p, vars);
            if (variableNamesOrNull != null) {
                for (String name : variableNamesOrNull) {
                    vars.put(name, null);
                }
            }
        }

        int databaseParametersCount = SystemProperties.getDatabaseParametersCount();
        if (!currentProcesses.isEmpty()) {
            for (val pp : Lists.partition(currentProcesses, databaseParametersCount)) {
                for (val v : currentDao.getVariablesImpl(pp, variableNamesOrNull)) {
                    result.get(v.getProcess()).put(v.getName(), v);
                }
            }
        }
        if (!archivedProcesses.isEmpty()) {
            for (val pp : Lists.partition(archivedProcesses, databaseParametersCount)) {
                for (val v : archivedDao.getVariablesImpl(pp, variableNamesOrNull)) {
                    result.get(v.getProcess()).put(v.getName(), v);
                }
            }
        }

        return result;
    }

    public List<? extends Variable> getVariablesByNameStartsWith(Process process, String namePrefix) {
        if (process.isArchived()) {
            return archivedDao
            .getVariablesByNameStartsWith((ArchivedProcess) process, namePrefix);
        } else {
            return currentDao.getVariablesByNameStartsWith((CurrentProcess) process, namePrefix);
        }
    }

    public boolean hasVariablesContainingValue(Long processId, String searchQuery) {
        return currentDao.hasVariablesContainingValue(processId, searchQuery);
    }
}
