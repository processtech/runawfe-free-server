package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddSubProcessIndexColumn extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_SUBPROCESS", new ColumnDef("SUBPROCESS_INDEX", dialect.getTypeName(Types.INTEGER))));
    }
}
