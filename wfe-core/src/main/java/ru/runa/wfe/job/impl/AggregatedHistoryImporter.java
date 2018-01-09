package ru.runa.wfe.job.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.dao.ProcessLogAwareDao;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.dao.Constant;
import ru.runa.wfe.commons.dao.ConstantDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDAO;

public class AggregatedHistoryImporter {
    protected final Log log = LogFactory.getLog(getClass());
    /**
     * Constant name to store process id
     */
    private final String importFromConstantName = "AggregatedProcessLogAwareDao.ImportFromId";

    @Autowired
    private ProcessLogAwareDao processLogAwareDao;

    @Autowired
    private ConstantDAO constantDao;

    @Autowired
    private ProcessLogDAO processLogDao;

    @Autowired
    private ProcessDAO processDao;

    @Transactional
    public void execute() {
        long processId = getProcessIdToImport();
        if (processId <= 0) {
            return;
        }
        try {
            log.info("Importing logs for process " + processId + " into aggregated logs.");
            Process process = processDao.get(processId);
            if (process != null) {
                Map<Long, Token> tokens = createTokensMap(process);
                List<ProcessLog> logs = processLogDao.getAll(processId);
                for (ProcessLog log : logs) {
                    processLogAwareDao.addLog(log, process, tokens.get(log.getTokenId()));
                }
            }
            saveProcessIdToImport(processId);
            log.info("Importing logs for process " + processId + " into aggregated logs is done.");
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Creates map from token id to token for process.
     *
     * @param process
     *            Process, which tokens must be transformed to map.
     * @return Returns map from token id to token.
     */
    private Map<Long, Token> createTokensMap(Process process) {
        Map<Long, Token> tokens = Maps.newHashMap();
        addTokenToMap(process.getRootToken(), tokens);
        return tokens;
    }

    /**
     * Add token to map and calling self recursive for all child tokens.
     *
     * @param token
     *            Token to add.
     * @param tokens
     *            Map from token id to token.
     */
    private void addTokenToMap(Token token, Map<Long, Token> tokens) {
        tokens.put(token.getId(), token);
        for (Token child : token.getChildren()) {
            addTokenToMap(child, tokens);
        }
    }

    /**
     * Get process id for history aggregation. Returns 0, if aggregation is not required.
     *
     * @return Returns process id for history aggregate.
     */
    private long getProcessIdToImport() {
        Constant importFromSettings = constantDao.get(importFromConstantName);
        if (importFromSettings == null || Strings.isNullOrEmpty(importFromSettings.getValue())) {
            DetachedCriteria criteria = DetachedCriteria.forClass(Process.class).addOrder(Order.desc("id"));
            List<Process> processes = processDao.getHibernateTemplate().findByCriteria(criteria, 0, 1);
            long processId = 0;
            if (processes != null && !processes.isEmpty()) {
                processId = processes.get(0).getId();
            }
            constantDao.create(new Constant(importFromConstantName, String.valueOf(processId)));
            importFromSettings = constantDao.get(importFromConstantName);
        }
        long processId = Long.parseLong(importFromSettings.getValue());
        return processId;
    }

    /**
     * Updates constant after history aggregation for some process.
     *
     * @param processId
     *            Process id, which history was aggregated.
     */
    private void saveProcessIdToImport(long processId) {
        Constant importFromSettings = constantDao.get(importFromConstantName);
        if (importFromSettings == null) {
            constantDao.create(new Constant(importFromConstantName, String.valueOf(processId)));
            importFromSettings = constantDao.get(importFromConstantName);
        }
        DetachedCriteria criteria = DetachedCriteria.forClass(Process.class).addOrder(Order.desc("id"));
        criteria.add(Restrictions.lt("id", processId));
        List<Process> processes = processDao.getHibernateTemplate().findByCriteria(criteria, 0, 1);
        if (processes != null && !processes.isEmpty()) {
            importFromSettings.setValue(String.valueOf(processes.get(0).getId()));
        } else {
            importFromSettings.setValue(String.valueOf(0));
        }
        constantDao.update(importFromSettings);
    }

}
