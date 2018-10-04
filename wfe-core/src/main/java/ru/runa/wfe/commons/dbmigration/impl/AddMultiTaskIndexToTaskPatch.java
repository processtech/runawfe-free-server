package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * 
 * Add index column in task for multi task element with element as discriminator value (not assigning task).
 * 
 * @author kanaal @since 4.2.1
 */
public class AddMultiTaskIndexToTaskPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_TASK", new ColumnDef("TASK_INDEX", dialect.getTypeName(Types.INTEGER), true)));
    }
}
