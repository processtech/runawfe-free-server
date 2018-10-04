package ru.runa.wfe.commons.dbmigration.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import lombok.val;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentTransitionLog;
import ru.runa.wfe.audit.dao.CurrentProcessLogDao;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.Transition;

public class TransitionLogPatch extends DbMigration {

    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private CurrentProcessDao currentProcessDao;
    @Autowired
    private CurrentProcessLogDao currentProcessLogDao;

    @Override
    public void executeDML(Session session) {
        String q;
        // JBPM_PASSTRANS
        q = "DELETE FROM JBPM_PASSTRANS WHERE TRANSITION_ID IS NULL OR NODE_ID IS NULL OR PROCESS_ID IS NULL";
        log.info("Deleted bad JBPM_PASSTRANS " + session.createSQLQuery(q).executeUpdate());
        Calendar limitDate = Calendar.getInstance();
        limitDate.add(Calendar.MONTH, -1);
        log.info("Deleting old transitions [before " + CalendarUtil.formatDate(limitDate) + "]");
        q = "DELETE FROM JBPM_PASSTRANS WHERE PROCESS_ID IN (SELECT ID FROM BPM_PROCESS WHERE END_DATE < :limitDate)";
        log.info("Deleted old JBPM_PASSTRANS " + session.createSQLQuery(q).setCalendar("limitDate", limitDate).executeUpdate());
        log.info("Processing graph history");
        q = "SELECT PROCESS_ID, NODE_ID, TRANSITION_ID FROM JBPM_PASSTRANS ORDER BY ID ASC";
        ScrollableResults scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        int failed = 0;
        int success = 0;
        val failedDeployments = new HashMap<ProcessDefinitionVersion, Date>();
        while (scrollableResults.next()) {
            CurrentProcess process = currentProcessDao.get(((Number) scrollableResults.get(0)).longValue());
            try {
                ParsedProcessDefinition definition = processDefinitionLoader.getDefinition(process);
                try {
                    Node node = definition.getNodeNotNull((String) scrollableResults.get(1));
                    Transition transition = node.getLeavingTransitionNotNull((String) scrollableResults.get(2));
                    CurrentTransitionLog transitionLog = new CurrentTransitionLog(transition);
                    transitionLog.setProcessId(process.getId());
                    transitionLog.setTokenId(process.getRootToken().getId());
                    transitionLog.setCreateDate(new Date());
                    currentProcessLogDao.create(transitionLog);
                    success++;
                } catch (Exception e) {
                    log.warn(e);
                    failed++;
                }
            } catch (InvalidDefinitionException e) {
                ProcessDefinitionVersion dv = process.getDefinitionVersion();
                if (failedDeployments.containsKey(dv)) {
                    Date endDate = failedDeployments.get(dv);
                    if (endDate != null && (process.getEndDate() == null || endDate.before(process.getEndDate()))) {
                        failedDeployments.put(dv, process.getEndDate());
                    }
                } else {
                    failedDeployments.put(dv, process.getEndDate());
                    log.error("Unable to restore history for " + dv + ": " + e);
                }
            }
        }
        log.info("-------------------- RESULT OF " + getClass());
        for (val entry : failedDeployments.entrySet()) {
            val dv = entry.getKey();
            val d = dv.getDefinition();
            log.warn("Unparsed definition " + d + " / " + dv + ", last process end date = " + entry.getValue());
        }
        log.info("Reverted history [for parsed definitions] result: success " + success + ", failed " + failed);
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(getDDLDropTable("JBPM_PASSTRANS"));
    }
}
