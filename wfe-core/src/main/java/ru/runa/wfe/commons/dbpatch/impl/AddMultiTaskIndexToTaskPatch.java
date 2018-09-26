package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * 
 * Add index column in task for multi task element with element as discriminator value (not assigning task).
 * 
 * @author kanaal @since 4.2.1
 */
public class AddMultiTaskIndexToTaskPatch extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_TASK", new ColumnDef("TASK_INDEX", dialect.getTypeName(Types.INTEGER), true)));
        return sql;
    }

}
