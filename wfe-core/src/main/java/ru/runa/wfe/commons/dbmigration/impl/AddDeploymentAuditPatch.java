package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Adds BPM_DEPLOYMENT (CREATE_USER_ID, UPDATE_DATE, UPDATE_USER_ID) columns.
 *
 * @since 4.3.0
 * @author Dofs
 */
public class AddDeploymentAuditPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("CREATE_USER_ID", dialect.getTypeName(Types.BIGINT), true)),
                getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("UPDATE_DATE", dialect.getTypeName(Types.DATE), true)),
                getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("UPDATE_USER_ID", dialect.getTypeName(Types.BIGINT), true))
        );
    }

    @Override
    public void executeDML(Session session) {
        session.createSQLQuery("DELETE FROM SYSTEM_LOG WHERE DISCRIMINATOR = 'PDUpd'").executeUpdate();
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLCreateForeignKey("BPM_PROCESS_DEFINITION", "FK_DEFINITION_CREATE_USER", "CREATE_USER_ID", "EXECUTOR", "ID"),
                getDDLCreateForeignKey("BPM_PROCESS_DEFINITION", "FK_DEFINITION_UPDATE_USER", "UPDATE_USER_ID", "EXECUTOR", "ID")
        );
    }
}
