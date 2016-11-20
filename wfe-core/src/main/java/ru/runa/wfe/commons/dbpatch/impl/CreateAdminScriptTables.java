package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.script.AdminScript;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.dao.ExecutorDAO;

public class CreateAdminScriptTables extends DBPatch {

    @Autowired
    protected ExecutorDAO executorDAO;
    @Autowired
    protected PermissionDAO permissionDAO;

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.addAll(createAdminScriptTable());
        return sql;
    }

    /**
     * Creates table, indexes e.t.c for {@link AdminScript}.
     *
     * @return Returns list of sql commands for table creation.
     */
    private List<String> createAdminScriptTable() {
        List<String> sql = new LinkedList<String>();
        List<ColumnDef> columns = new LinkedList<DBPatch.ColumnDef>();
        ColumnDef id = new ColumnDef("ID", Types.BIGINT, false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("CONTENT", dialect.getTypeName(Types.CLOB), false));
        sql.add(getDDLCreateTable("ADMIN_SCRIPT", columns, null));
        sql.add(getDDLCreateSequence("SEQ_ADMIN_SCRIPT"));
        sql.add(getDDLCreateUniqueKey("ADMIN_SCRIPT", "IX_ADMIN_SCRIPT_NAME", "NAME"));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
