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

    @Autowired
    private SessionFactory sessionFactory;

    @Transactional
    public void execute() {
        // TODO ...
        // TODO Analyze also token.execution_status.
        // Including subprocesses.
        val processIdsToMove = new ArrayList<Long>();

        log.debug("processIdsToMove.size() = " + processIdsToMove.size());
        if (processIdsToMove.isEmpty()) {
            return;
        }

        val session = sessionFactory.getCurrentSession();
        for (val pids : Lists.partition(processIdsToMove, 100)) {
            val pidsString = StringUtils.join(pids, ",");
            session.createSQLQuery("insert into archived_process " +
                    "      (id, parent_id, tree_path, start_date, end_date, version, definition_id, root_token_id) " +
                    "select id, parent_id, tree_path, start_date, end_date, version, definition_id, root_token_id " +
                    "from bpm_process " +
                    "where id in (" + pidsString + ")"
            ).executeUpdate();

            // TODO log
            // TODO swimlane
            // TODO variable
            // TODO token
            // TODO subprocess
        }
    }
}
