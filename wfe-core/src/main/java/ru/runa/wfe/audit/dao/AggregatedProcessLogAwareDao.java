package ru.runa.wfe.audit.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.commons.dao.CommonDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

@Component
public class AggregatedProcessLogAwareDao extends CommonDao implements ProcessLogAwareDao {

    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    @Override
    public void addLog(ProcessLog processLog, Process process, Token token) {
        UpdateAggregatedLogOperation op = new UpdateAggregatedLogOperation(sessionFactory, queryFactory, processDefinitionLoader, process, token);
        processLog.processBy(op);
    }
}
