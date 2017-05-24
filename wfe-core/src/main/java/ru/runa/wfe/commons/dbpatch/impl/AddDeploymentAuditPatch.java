package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

/**
 * Adds BPM_DEPLOYMENT (CREATE_USER_ID, UPDATE_DATE, UPDATE_USER_ID) columns.
 *
 * @since 4.3.0
 * @author Dofs
 */
public class AddDeploymentAuditPatch extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("CREATE_USER_ID", dialect.getTypeName(Types.BIGINT), true)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("UPDATE_DATE", dialect.getTypeName(Types.DATE), true)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("UPDATE_USER_ID", dialect.getTypeName(Types.BIGINT), true)));
        return sql;
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateForeignKey("BPM_PROCESS_DEFINITION", "FK_DEFINITION_CREATE_USER", "CREATE_USER_ID", "EXECUTOR", "ID"));
        sql.add(getDDLCreateForeignKey("BPM_PROCESS_DEFINITION", "FK_DEFINITION_UPDATE_USER", "UPDATE_USER_ID", "EXECUTOR", "ID"));
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        session.createSQLQuery("DELETE FROM SYSTEM_LOG WHERE DISCRIMINATOR = 'PDUpd'").executeUpdate();
    }

}
