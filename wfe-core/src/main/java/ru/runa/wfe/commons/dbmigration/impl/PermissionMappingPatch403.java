package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class PermissionMappingPatch403 extends DbMigration {

    @Override
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLDropIndex("PERMISSION_MAPPING", "IX_PERMISSION_BY_IDENTIFIABLE"),
                getDDLCreateUniqueKey("PERMISSION_MAPPING", "UQ_MAPPINGS", "IDENTIFIABLE_ID", "TYPE_ID", "MASK", "EXECUTOR_ID")
        );
    }
}
