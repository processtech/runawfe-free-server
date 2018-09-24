package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddSubprocessBindingDatePatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("SUBPROCESS_BINDING_DATE", dialect.getTypeName(Types.TIMESTAMP))));
    }
}