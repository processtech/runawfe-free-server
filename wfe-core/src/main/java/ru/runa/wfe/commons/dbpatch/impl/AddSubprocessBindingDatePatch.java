package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddSubprocessBindingDatePatch extends DBPatch {
    @Autowired
    private ProcessLogDAO processLogDAO;

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("SUBPROCESS_BINDING_DATE", dialect.getTypeName(Types.TIMESTAMP))));
        return sql;
    }

}