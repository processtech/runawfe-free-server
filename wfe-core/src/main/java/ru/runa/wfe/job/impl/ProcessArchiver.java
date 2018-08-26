package ru.runa.wfe.job.impl;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.SystemProperties;

@CommonsLog
public class ProcessArchiver {
    private static final int ROOT_PROCESS_IDS_PER_STEP = 100;
    private static final int IDS_PER_INSERT = 100;

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * First run on huge database may take long time, so prevent concurrent runs just in case.
     */
    private boolean busy = false;

    /**
     * If false, execute() does nothing.
     */
    private boolean permanentFailure = false;

    /**
     * Contains two "?" params:
     * <ol>
     *     <li>last process ID processed, so next step continues where previous step stopped (MS SQL does not support OFFSET+LIMIT, only LIMIT);
     *     <li>LIMIT value.</li>
     * </ol>
     */
    private String sqlSelectRootProcessIds = null;

    private String sqlSelectSubProcessIds = null;

    public void execute() {
        log.warn("NOOP for debug, processDefaultEndedSecondsBeforeArchiving() = " + SystemProperties.getProcessDefaultEndedSecondsBeforeArchiving());
        if (true) return;

        if (!SystemProperties.isProcessArchivingEnabled() || permanentFailure || busy) {
            return;
        }
        busy = true;
        try {
            generateSqls();
            //noinspection StatementWithEmptyBody
            while (step());
        } finally {
            busy = false;
        }
    }

    private void generateSqls() {
        if (sqlSelectRootProcessIds != null && sqlSelectSubProcessIds != null) {
            return;
        }

        Dialect dialect = ApplicationContextFactory.getDialect();
        if (!dialect.supportsLimit()) {
            permanentFailure = true;
            throw new RuntimeException("Current database dialect " + dialect + " does not support LIMIT");
        }

        // There is no date / time / timestamp arithmetic in QueryDSL, neither in HQL / JPA. Must fallback to SQL.
        // And since this arithmetic is different for different SQL servers, have to switch on server type.
        DbType dbType = ApplicationContextFactory.getDBType();
        int defaultEndedSecondsBeforeArchiving = SystemProperties.getProcessDefaultEndedSecondsBeforeArchiving();

        // Since we don't have true tree closure with (root_id, root_id, 0) record,
        // we must check archiving condition separately for root processes and their subprocesses.
        sqlSelectRootProcessIds = dialect.getLimitString("select p.id " +
                "from bpm_process p " +
                "inner join bpm_process_definition d on (d.id = p.definition_id) " +
                // Continue since last step:
                "where p.id > ? and " +
                // Get only root process IDs:
                "      not exists (select s.process_id from bpm_subprocess where s.process_id = p.id) and " +
                // Check condition for root processes:
                "      " + generateEndDateCheckExpression("d", "p.end_date", dbType, defaultEndedSecondsBeforeArchiving) + " and " +
                "      not exists (select t.process_id from bpm_task t where t.process_id = p.id) and " +
                "      not exists (select j.process_jd from bpm_job j where j.process_id = p.id) and " +
                // Check no descendant processes exist that violate condition:
                "      not exists (" +
                "          select p2.id " +
                "          from bpm_subprocess s2 " +
                "          inner join bpm_process p2 on (p2.id = s2.process_id) " +
                "          inner join bpm_process_definition d2 on (d2.id = p2.definition_id) " +
                "          where s2.root_process_id = p.id and (" +
                "                not(" + generateEndDateCheckExpression("d2", "p2.end_date", dbType, defaultEndedSecondsBeforeArchiving) + ") or " +
                "                exists (select t.process_id from bpm_task t where t.process_id = p2.id) or " +
                "                exists (select j.process_jd from bpm_job j where j.process_id = p2.id) " +
                "          ) " +
                "      )" +
                "order by p.id", 0, ROOT_PROCESS_IDS_PER_STEP);



        sqlSelectSubProcessIds = "select distinct s.root_process_id, p.id " +
                "from bpm_process p " +
                "inner join bpm_process_definition d on (d.id = p.definition_id) " +
                "inner join bpm_subprocess s on (s.root_process_id = ?)" +
                "where p.execution_state = 'ENDED' ";  // TODO ...
    }

    /**
     * Returned expression contains single "?" parameter for current time.
     * I could use NOW(), but Java and SQL timezones may differ, and END_DATE values are set by Java code when entities are stored.
     *
     * @param definitionAlias E.g. "d".
     * @param endDateField E.g. "p.end_date" for process, or "t.end_date" for token.
     */
    private String generateEndDateCheckExpression(
            String definitionAlias, String endDateField, DbType dbType, int defaultEndedSecondsBeforeArchiving
    ) {
        // COALESCE function is the same in all supported SQL servers.
        val seconds = "coalesce(" + definitionAlias + ".ended_seconds_before_archiving, " + defaultEndedSecondsBeforeArchiving + ")";

        switch (dbType) {
            case H2:
            case HSQL:
            case MSSQL:
                return "dateadd('second', " + seconds + ", " + endDateField + ") < ?";
            case MYSQL:
                return "date_add(" + endDateField + ", interval " + seconds + " second) < ?";
            case ORACLE:
                return "(" + endDateField + " + interval '" + seconds + "' second) < ?";
            case POSTGRESQL:
                return "(" + endDateField + " + interval '" + seconds + " second') < ?";
            default:
                permanentFailure = true;
                throw new RuntimeException("Unsupported dbType = " + dbType);
        }
    }

    /**
     * First run on huge database may take a long time, so instead of single huge transaction, we'll go in smaller transactional steps.
     *
     * @return False if complete.
     */
    @Transactional
    public boolean step() {

        // TODO ...
        // TODO Analyze also token.execution_status.
        // Including subprocesses.
        val processIdsToArchive = new ArrayList<Long>();

        log.debug("step(): processIdsToArchive.size() = " + processIdsToArchive.size());
        if (processIdsToArchive.isEmpty()) {
            return false;
        }

        val session = sessionFactory.getCurrentSession();
        for (val pids : Lists.partition(processIdsToArchive, IDS_PER_INSERT)) {
            val pidsCSV = "(" + StringUtils.join(pids, ",") + ")";

            // Create rows in referenced tables first, then in referencing tables.

            // Refernces self, plus has root_token_id field.
            session.createSQLQuery("insert into archived_process " +
                    "      (id, parent_id, tree_path, start_date, end_date, version, definition_id, root_token_id) " +
                    "select id, parent_id, tree_path, start_date, end_date, version, definition_id, root_token_id " +
                    "from bpm_process " +
                    "where id in " + pidsCSV
            ).executeUpdate();

            // References process and self.
            session.createSQLQuery("insert into archived_token " +
                    "      (id, process_id, parent_id, error_message, transition_id, message_selector, start_date, end_date, error_date, node_id, reactivate_parent, node_type, version, name) " +
                    "select id, process_id, parent_id, error_message, transition_id, message_selector, start_date, end_date, error_date, node_id, reactivate_parent, node_type, version, name " +
                    "from bpm_token " +
                    "where process_id in " + pidsCSV
            ).executeUpdate();

            // References process, also has parent_token_id field.
            session.createSQLQuery("insert into archived_subprocess " +
                    "      (id, process_id, parent_process_id, parent_node_id, create_date, subprocess_index, parent_token_id) " +
                    "select id, process_id, parent_process_id, parent_node_id, create_date, subprocess_index, parent_token_id " +
                    "from bpm_subprocess " +
                    "where process_id in " + pidsCSV
            ).executeUpdate();

            // References process.
            session.createSQLQuery("insert into archived_swimlane " +
                    "      (id, process_id, create_date, name, version, executor_id) " +
                    "select id, process_id, create_date, name, version, executor_id " +
                    "from bpm_swimlane " +
                    "where process_id in " + pidsCSV
            ).executeUpdate();

            // References process.
            session.createSQLQuery("insert into archived_variable " +
                    "      (discriminator, id, process_id, create_date, name, version, converter, bytes, stringvalue, longvalue, doublevalue, datevalue) " +
                    "select discriminator, id, process_id, create_date, name, version, converter, bytes, stringvalue, longvalue, doublevalue, datevalue " +
                    "from bpm_variable " +
                    "where process_id in " + pidsCSV
            ).executeUpdate();

            // No FKs, but has process_id and token_id fields.
            session.createSQLQuery("insert into archived_log " +
                    "      (discriminator, id, process_id, node_id, token_id, create_date, severity, bytes, content) " +
                    "select discriminator, id, process_id, node_id, token_id, create_date, severity, bytes, content " +
                    "from bpm_log " +
                    "where process_id in " + pidsCSV
            ).executeUpdate();

            // Delete rows in reverse order (from referencing tables first):

            // No FKs, but has process_id and token_id fields.
            session.createSQLQuery("delete from bpm_log where process_id in " + pidsCSV).executeUpdate();

            // References process.
            session.createSQLQuery("delete from bpm_variable where process_id in " + pidsCSV).executeUpdate();

            // References process.
            session.createSQLQuery("delete from bpm_swimlane where process_id in " + pidsCSV).executeUpdate();

            // References process and token.
            session.createSQLQuery("delete from bpm_subprocess where process_id in " + pidsCSV).executeUpdate();

            // References process and self.
            // Also, since process references token, must null that references before deleting tokens.
            session.createSQLQuery("update bpm_token set parent_id = null where process_id in " + pidsCSV).executeUpdate();
            session.createSQLQuery("update bpm_process set root_token_id = null where id in " + pidsCSV).executeUpdate();
            session.createSQLQuery("delete from bpm_token where process_id in " + pidsCSV).executeUpdate();

            // References token (already deleted above) and self.
            session.createSQLQuery("update bpm_process set parent_id = null where id in " + pidsCSV).executeUpdate();
            session.createSQLQuery("delete from bpm_process where id in " + pidsCSV).executeUpdate();
        }

        return true;
    }
}
