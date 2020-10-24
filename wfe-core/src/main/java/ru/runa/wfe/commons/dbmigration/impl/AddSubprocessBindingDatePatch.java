package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddSubprocessBindingDatePatch extends DbMigration {
    @Autowired
    private ProcessLogDao processLogDao;

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("SUBPROCESS_BINDING_DATE", dialect.getTypeName(Types.TIMESTAMP))));
        return sql;
    }

}