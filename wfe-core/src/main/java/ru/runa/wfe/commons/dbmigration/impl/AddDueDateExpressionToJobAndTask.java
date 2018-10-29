package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddDueDateExpressionToJobAndTask extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("BPM_JOB", new ColumnDef("DUE_DATE_EXPRESSION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)),
                getDDLCreateColumn("BPM_TASK", new ColumnDef("DEADLINE_DATE_EXPRESSION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true))
        );
    }
}
