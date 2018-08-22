package ru.runa.wfe.job.impl;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
public class ProcessArchiver {
    private static final int IDS_PER_STEP = 1000;
    private static final int IDS_PER_INSERT = 100;

    @Autowired
    private SessionFactory sessionFactory;

    // First run on huge database may take long time, so prevent concurrent runs just in case.
    private boolean busy = false;

    public void execute() {
        if (busy) {
            return;
        }
        busy = true;
        try {
            //noinspection StatementWithEmptyBody
            while (step());
        } finally {
            busy = false;
        }
    }

    /**
     * First run on huge database may take a long time, so instead of single huge transaction, process in smaller transactional steps.
     *
     * @return False if complete.
     */
    @Transactional
    public boolean step() {
        // TODO ...
        // TODO Analyze also token.execution_status.
        // Including subprocesses.
        val processIdsToMove = new ArrayList<Long>();

        log.debug("step(): processIdsToMove.size() = " + processIdsToMove.size());
        if (processIdsToMove.isEmpty()) {
            return false;
        }

        val session = sessionFactory.getCurrentSession();
        for (val pids : Lists.partition(processIdsToMove, IDS_PER_INSERT)) {
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
