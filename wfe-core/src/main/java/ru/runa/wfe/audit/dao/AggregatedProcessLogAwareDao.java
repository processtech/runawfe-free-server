package ru.runa.wfe.audit.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.commons.dao.CommonDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;

@Component
public class AggregatedProcessLogAwareDao extends CommonDao implements ProcessLogAwareDao {

    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    @Override
    public void addLog(BaseProcessLog processLog, CurrentProcess process, CurrentToken token) {
        UpdateAggregatedLogOperation op = new UpdateAggregatedLogOperation(sessionFactory, queryFactory, processDefinitionLoader, process, token);
        processLog.processBy(op);
    }
}
