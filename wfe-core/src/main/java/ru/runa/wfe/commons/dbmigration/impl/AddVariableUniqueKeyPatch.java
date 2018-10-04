package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddVariableUniqueKeyPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeDDL(getDDLCreateUniqueKey("BPM_VARIABLE", "UK_VARIABLE_PROCESS", "PROCESS_ID", "NAME"));
    }
}
