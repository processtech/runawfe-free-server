package ru.runa.wfe.audit.dao;

import java.util.List;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.commons.dao.IGenericDAO;
import ru.runa.wfe.lang.ProcessDefinition;

public interface IProcessLogDAO<T extends ProcessLog> extends IGenericDAO<T> {

    /**
     * @return process logs for embedded subprocess or for main process without
     *         embedded subprocesses.
     */
    public List<T> get(Long processId, ProcessDefinition definition);

    /**
     * @return process logs.
     */
    public List<T> getAll(Long processId);

    /**
     * @return process logs.
     */
    public List<T> getAll(final ProcessLogFilter filter);

}
