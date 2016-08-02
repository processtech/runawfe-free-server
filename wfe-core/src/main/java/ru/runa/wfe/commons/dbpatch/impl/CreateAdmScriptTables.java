package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.commons.dbpatch.DBPatch.ColumnDef;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Lists;

public class CreateAdmScriptTables extends DBPatch {

    @Autowired
    protected ExecutorDAO executorDAO;
    @Autowired
    protected PermissionDAO permissionDAO;

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.addAll(createLobStorageTable());
        sql.addAll(createAdmScriptTable());
        sql.add(getDDLCreateForeignKey("ADM_SCRIPT", "FK_ADM_SCRIPT_LOB_STORAGE", "STORAGE_ID", "LOB_STORAGE", "ID"));
        return sql;
    }

    /**
     * Creates table, indexes e.t.c for {@link LobStorage}.
     *
     * @return Returns list of sql commands for table creation.
     */
    private List<String> createLobStorageTable() {
        List<String> sql = new LinkedList<String>();
        List<ColumnDef> columns = new LinkedList<DBPatch.ColumnDef>();
        ColumnDef id = new ColumnDef("ID", Types.BIGINT, false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("VALUE", dialect.getTypeName(Types.LONGVARCHAR), false));
        sql.add(getDDLCreateTable("LOB_STORAGE", columns, null));
        sql.add(getDDLCreateSequence("SEQ_STORAGE"));
        return sql;
    }

    /**
     * Creates table, indexes e.t.c for {@link AdmScript}.
     *
     * @return Returns list of sql commands for table creation.
     */
    private List<String> createAdmScriptTable() {
        List<String> sql = new LinkedList<String>();
        List<ColumnDef> columns = new LinkedList<DBPatch.ColumnDef>();
        ColumnDef id = new ColumnDef("ID", Types.BIGINT, false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("STORAGE_ID", Types.BIGINT, false));
        sql.add(getDDLCreateTable("ADM_SCRIPT", columns, null));
        sql.add(getDDLCreateSequence("SEQ_ADM_SCRIPT"));
        sql.add(getDDLCreateUniqueKey("ADM_SCRIPT", "IX_ADM_SCRIPT_NAME", "NAME"));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
    }
}
