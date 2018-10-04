package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import lombok.val;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Called BEFORE SplitProcessDefinitionVersion: table BPM_PROCESS_DEFINITION does not have UK(name, version), so perform sanity check first.
 *
 * Implemented as separate migration, since no DMLs are allowed in executeDDLBefore().
 */
public class SplitProcessDefinitionVersionCheck extends DbMigration {

    @Override
    @SuppressWarnings("ConstantConditions")
    public void executeDML(Connection conn) throws Exception {

        @SuppressWarnings("unchecked")
        val rs = conn.createStatement()
                .executeQuery("select name, version, count(*) from bpm_process_definition group by name, version having count(*) > 1");
        if (!rs.next()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Duplicated (name,version): ");
        boolean first = true;
        do {
            String name = rs.getString(1);
            Long version = rs.getLong(2);
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append("(").append(name).append(",").append(version).append(")");
        } while (rs.next());
        throw new RuntimeException(sb.toString());
    }
}
