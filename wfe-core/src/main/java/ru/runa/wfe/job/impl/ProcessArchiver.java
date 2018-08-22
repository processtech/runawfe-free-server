package ru.runa.wfe.job.impl;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.Expressions;
import java.util.ArrayList;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;
import ru.runa.wfe.definition.QDeployment;
import ru.runa.wfe.execution.QCurrentProcess;
import ru.runa.wfe.execution.QCurrentToken;

@CommonsLog
public class ProcessArchiver {
    private static final int IDS_PER_STEP = 1000;
    private static final int IDS_PER_INSERT = 100;

    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private HibernateQueryFactory queryFactory;

    // First run on huge database may take long time, so prevent concurrent runs just in case.
    private boolean busy = false;

    // Lazily initialized:
    private String sqlSelectProcessIdsToArchive = null;

    public void execute() {
        if (busy) {
            return;
        }
        busy = true;
        try {
            if (sqlSelectProcessIdsToArchive == null) {
                sqlSelectProcessIdsToArchive = generateSql();
            }

            //noinspection StatementWithEmptyBody
            while (step());
        } finally {
            busy = false;
        }
    }

    private String generateSql() {
        DbType dbType = ApplicationContextFactory.getDBType();

        val defaultEndedSecondsBeforeArchiving = SystemProperties.getProcessDefaultEndedSecondsBeforeArchiving();

    }

    /**
     * Returned expression contains single "?" parameter for current time.
     * I could use NOW(), but Java and SQL timezones may differ, and END_DATE values are set by Java code.
     *
     * @param field "p.end_date" for process, "t.end_date" for token.
     */
    private String generateEndedTimeCheckExpression(String field) {

        // There is NO date / time / timestamp arithmetic in QueryDSL, neither in HQL / JPA. Must fallback to SQL.
        // And since this arithmetic is different for different SQL servers, have to switch on server type.
        val dbType = ApplicationContextFactory.getDBType();

        // COALESCE function is the same in all supported SQL servers.
        val defaultEndedSecondsBeforeArchiving = SystemProperties.getProcessDefaultEndedSecondsBeforeArchiving();
        val seconds = "coalesce(d.ended_seconds_before_archiving, " + defaultEndedSecondsBeforeArchiving + ")";

        switch (dbType) {
            case H2:
                return "dateadd('second', " + seconds + ", " + field;
            case HSQL:
                break;
            case MSSQL:
                break;
            case MYSQL:
                break;
            case ORACLE:
                break;
            case POSTGRESQL:
                break;
            default:
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
        // There is NO date / time / timestamp arithmetic in QueryDSL, neither in HQL / JPA. So, must fallback to SQL.
        // And since in this arithmetic is different for different servers, have to switch on server type.
        DbType dbType = ApplicationContextFactory.getDBType();

        val defaultEndedSecondsBeforeArchiving = SystemProperties.getProcessDefaultEndedSecondsBeforeArchiving();
        String canArchiveExpr;

        val d = QDeployment.deployment;
        val p = QCurrentProcess.currentProcess;
        val t = QCurrentToken.currentToken;

        val defaultEndedSecondsBeforeArchiving = SystemProperties.getProcessDefaultEndedSecondsBeforeArchiving();
        val canArchiveExpr = p.endDate le(Expressions. Expressions.currentTimestamp(). d.endedDaysBeforeArchiving.coalesce(defaultEndedSecondsBeforeArchiving);

        queryFactory.selectDistinct(p.id).from(p).innerJoin(p.deployment, d)


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
