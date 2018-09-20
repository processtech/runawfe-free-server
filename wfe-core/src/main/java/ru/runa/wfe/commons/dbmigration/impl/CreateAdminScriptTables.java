package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.script.AdminScript;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.dao.ExecutorDao;

public class CreateAdminScriptTables extends DbMigration {

    @Autowired
    protected ExecutorDao executorDao;
    @Autowired
    protected PermissionDao permissionDao;

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
        val sql = new LinkedList<String>();
        val columns = new LinkedList<DbMigration.ColumnDef>();
        columns.add(new BigintColumnDef("ID", false).setPrimaryKey());
        columns.add(new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("CONTENT", dialect.getTypeName(Types.BLOB), false));
        sql.add(getDDLCreateTable("ADMIN_SCRIPT", columns, null));
        sql.add(getDDLCreateSequence("SEQ_ADMIN_SCRIPT"));
        sql.add(getDDLCreateUniqueKey("ADMIN_SCRIPT", "IX_ADMIN_SCRIPT_NAME", "NAME"));
        return sql;
    }

}
