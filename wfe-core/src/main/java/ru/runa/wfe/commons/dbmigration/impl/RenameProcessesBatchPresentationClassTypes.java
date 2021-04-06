package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import java.util.ArrayList;
import lombok.val;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.util.Pair;

/**
 * See TNMS #5151 comment 37.
 */
public class RenameProcessesBatchPresentationClassTypes extends DbMigration {

    @Override
    public void executeDML(Connection conn) throws Exception {
        val replacements = new ArrayList<Pair<String,String>>() {{
            add(new Pair<>("PROCESS", "CURRENT_PROCESS"));
            add(new Pair<>("PROCESS_WITH_TASKS", "CURRENT_PROCESS_WITH_TASKS"));
        }};

        try (val stmt = conn.prepareStatement("update batch_presentation set class_type = ? where class_type = ?")) {
            for (val r : replacements) {
                stmt.setString(1, r.getValue2());
                stmt.setString(2, r.getValue1());
                stmt.executeUpdate();
            }
        }
    }
}
