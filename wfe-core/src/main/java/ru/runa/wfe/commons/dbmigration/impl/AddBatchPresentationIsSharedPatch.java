package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import org.hibernate.Session;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddBatchPresentationIsSharedPatch extends DbMigration {
    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BATCH_PRESENTATION", new ColumnDef("SHARED", dialect.getTypeName(Types.BIT), true)));
    }

    @Override
    public void executeDML(Session session) {
        String initialValue = dbType == DbType.ORACLE ? "0" : "FALSE";
        session.createSQLQuery("UPDATE BATCH_PRESENTATION SET SHARED = " + initialValue).executeUpdate();
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(getDDLModifyColumnNullability("BATCH_PRESENTATION", "SHARED", dialect.getTypeName(Types.BIT), false));
    }
}
