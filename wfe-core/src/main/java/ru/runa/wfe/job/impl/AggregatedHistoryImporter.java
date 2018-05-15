package ru.runa.wfe.job.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.dao.ProcessLogAwareDao;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.dao.Constant;
import ru.runa.wfe.commons.dao.ConstantDAO;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.QProcess;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDAO;

/**
 * Loading process history one by one and aggregate it to aggregated logs.
 */
public class AggregatedHistoryImporter extends TransactionalExecutor {

    private static final Log log = LogFactory.getLog(AggregatedHistoryImporter.class);
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
    @Autowired
    private HibernateQueryFactory queryFactory;

    @Override
    protected void doExecuteInTransaction() throws Exception {
        long processId = getProcessIdToImport();
        if (processId <= 0) {
            return;
        }
        log.info("Importing logs for process " + processId + " into aggregated logs.");
        Process process = processDao.get(processId);
        if (process != null) {
            Map<Long, Token> tokens = createTokensMap(process);
            List<ProcessLog> logs = processLogDao.getAll(processId);
            for (ProcessLog log : logs) {
                // try {
                processLogAwareDao.addLog(log, process, tokens.get(log.getTokenId()));
                // } catch (Exception e) {
                // AggregatedHistoryImporter.log.warn("Ignoring error on log aggregation for log instance "
                // + log.getId(), e);
                // }
            }
        }
        saveProcessIdToImport(processId);
        log.info("Importing logs for process " + processId + " into aggregated logs is done.");
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
     * Get process id for history aggregation. Returns 0, if aggregation is not
     * required.
     *
     * @return Returns process id for history aggregate.
     */
    private long getProcessIdToImport() {
        Constant importFromSettings = constantDao.get(importFromConstantName);
        if (importFromSettings == null || Strings.isNullOrEmpty(importFromSettings.getValue())) {
            QProcess p = QProcess.process;
            Long processId = queryFactory.select(p.id).from(p).orderBy(p.id.desc()).fetchFirst();
            if (processId == null) {
                processId = 0L;
            }
            constantDao.create(new Constant(importFromConstantName, String.valueOf(processId)));
            importFromSettings = constantDao.get(importFromConstantName);
        }
        return Long.parseLong(importFromSettings.getValue());
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
        QProcess p = QProcess.process;
        Long foundId = queryFactory.select(p.id).from(p).where(p.id.lt(processId)).orderBy(p.id.desc()).fetchFirst();
        importFromSettings.setValue(String.valueOf(foundId != null ? foundId : 0L));
        constantDao.update(importFromSettings);
    }
}
