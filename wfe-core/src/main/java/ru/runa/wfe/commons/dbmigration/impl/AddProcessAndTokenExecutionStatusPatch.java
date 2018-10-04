package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * @author Alex Chernyshev
 */
public class AddProcessAndTokenExecutionStatusPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("BPM_PROCESS", new ColumnDef("EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))),
                getDDLCreateColumn("BPM_TOKEN", new ColumnDef("EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255)))
        );
    }

    @Override
    public void executeDML(Session session) {
        session.createSQLQuery("UPDATE BPM_PROCESS SET EXECUTION_STATUS='ENDED' WHERE END_DATE IS NOT NULL").executeUpdate();
        session.createSQLQuery("UPDATE BPM_PROCESS SET EXECUTION_STATUS='ACTIVE' WHERE END_DATE IS NULL").executeUpdate();
        session.createSQLQuery("UPDATE BPM_TOKEN SET EXECUTION_STATUS='ENDED' WHERE END_DATE IS NOT NULL").executeUpdate();
        session.createSQLQuery("UPDATE BPM_TOKEN SET EXECUTION_STATUS='ACTIVE' WHERE END_DATE IS NULL").executeUpdate();
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLModifyColumnNullability("BPM_PROCESS", "EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false),
                getDDLModifyColumnNullability("BPM_TOKEN", "EXECUTION_STATUS", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), false)
        );
    }
}
