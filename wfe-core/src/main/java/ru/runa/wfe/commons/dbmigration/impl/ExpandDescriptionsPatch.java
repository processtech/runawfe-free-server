package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class ExpandDescriptionsPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeDDL(
                getDDLModifyColumn("BPM_TASK", "DESCRIPTION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)),
                getDDLModifyColumn("BPM_PROCESS_DEFINITION", "DESCRIPTION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)),
                getDDLModifyColumn("EXECUTOR_RELATION", "DESCRIPTION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024)),
                getDDLModifyColumn("EXECUTOR", "DESCRIPTION", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))
        );
    }
}
