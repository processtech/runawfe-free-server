package ru.runa.wfe.audit.dao;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.commons.dao.ConstantDAO;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public class AggregatedProcessLogAwareDao extends CommonDAO implements ProcessLogAwareDao {

    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;

    @Autowired
    private ConstantDAO constantDao;

    @Autowired
    private ProcessLogDAO processLogDao;

    @Override
    public void addLog(ProcessLog processLog, Process process, Token token) {
        UpdateAggregatedLogOperation operation = new UpdateAggregatedLogOperation(getHibernateTemplate(), processDefinitionLoader, process, token);
        processLog.processBy(operation);
    }
}
