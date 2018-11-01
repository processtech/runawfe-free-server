package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddColumnsToSubstituteEscalatedTasksPatch extends DbMigration {

    private static final Pattern DECIMAL_LONG = Pattern.compile("([\\d]+)");

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("EXECUTOR", new BigintColumnDef("PROCESS_ID")),
                getDDLCreateColumn("EXECUTOR", new VarcharColumnDef("NODE_ID", 255))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void executeDML(Session session) {
        List<Object[]> bundles = session.createSQLQuery("SELECT ID, DESCRIPTION FROM EXECUTOR WHERE DISCRIMINATOR IN ('E', 'T')").list();

        for (Object[] bundle : bundles) {
            try {
                log.info(String.format("applyPatch: id: %s set PROCESS_ID description: %s", bundle[0], bundle[1]));
                Long pid = 0L;
                Matcher m;
                if (bundle[1] != null && ((m = DECIMAL_LONG.matcher((CharSequence) bundle[1])).find())) {
                    MatchResult mr = m.toMatchResult();
                    if (mr.groupCount() >= 1) {
                        try {
                            pid = Long.parseLong(mr.group(1));
                        } catch (Exception e) {
                            /* skip if process with pid is not found */
                        }
                    }
                }
                session.createSQLQuery(String.format("UPDATE EXECUTOR SET PROCESS_ID=%s WHERE ID=%s", pid, bundle[0])).executeUpdate();
            } catch (Exception e) {
                log.warn(String.format("applyPatch: set PROCESS_ID for id: %s exc: %s", bundle[0], e));
            }
        }

        List<Number> ids = session.createSQLQuery("SELECT ID FROM EXECUTOR WHERE DISCRIMINATOR IN ('E')").list();

        for (Number id : ids) {
            log.info(String.format("applyPatch: id: %s set NODE_ID", id));
            try {
                session.createSQLQuery(String.format("UPDATE EXECUTOR SET NODE_ID=NULL WHERE ID=%s", id)).executeUpdate();
            } catch (Exception e) {
                log.warn(String.format("applyPatch: set NODE_ID for id: %s exc: %s", id, e));
            }
        }
    }
}
