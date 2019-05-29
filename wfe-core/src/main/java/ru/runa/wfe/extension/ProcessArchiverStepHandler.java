package ru.runa.wfe.extension;

import java.sql.Connection;
import java.util.ArrayList;

public interface ProcessArchiverStepHandler {

    /**
     * Called in the middle of transactional method ProcessArchived.step(),
     * after inserting processes to archive but before deleting them from current tables.
     * <p>
     * If handler throws, whole step() will be rolled back.
     *
     * @param conn Caller's working connection, with active transaction.
     * @param processIds List of IDs being archived in current step() call.
     * @param processIdsCsv Same list, formatted as "(1,2,3)".
     */
    void handle(Connection conn, ArrayList<Long> processIds, String processIdsCsv) throws Exception;
}
