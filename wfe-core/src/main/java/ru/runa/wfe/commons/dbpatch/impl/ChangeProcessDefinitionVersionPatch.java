package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class ChangeProcessDefinitionVersionPatch extends DBPatch {
    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLRenameColumn("BPM_PROCESS_DEFINITION", "VERSION", new ColumnDef("VERSION_", Types.BIGINT)));
        sql.add(getDDLCreateColumn("BPM_PROCESS_DEFINITION", new ColumnDef("VERSION", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));

        sql.add(getDDLRenameColumn("SYSTEM_LOG", "PROCESS_DEFINITION_VERSION", new ColumnDef("PROCESS_DEFINITION_VERSION_", Types.BIGINT)));
        sql.add(getDDLCreateColumn("SYSTEM_LOG", new ColumnDef("PROCESS_DEFINITION_VERSION", dialect.getTypeName(Types.VARCHAR, 255, 255, 255))));
        return sql;
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        final List<?> pdList = session.createSQLQuery("SELECT ID, VERSION_ FROM BPM_PROCESS_DEFINITION").list();
        for (final Object results : pdList) {
            if (results instanceof Object[]) {
                final Object[] row = (Object[]) results;
                final SQLQuery query = session.createSQLQuery("UPDATE BPM_PROCESS_DEFINITION SET VERSION=:version WHERE ID=:id");
                query.setParameter("id", row[0]);
                query.setParameter("version", row[1].toString());
                query.executeUpdate();
            }
        }

        final List<?> slList = session
                .createSQLQuery("SELECT ID, PROCESS_DEFINITION_VERSION_ FROM SYSTEM_LOG WHERE PROCESS_DEFINITION_VERSION_ IS NOT NULL").list();
        for (final Object results : slList) {
            if (results instanceof Object[]) {
                final Object[] row = (Object[]) results;
                final SQLQuery query = session.createSQLQuery("UPDATE SYSTEM_LOG SET PROCESS_DEFINITION_VERSION=:version WHERE ID=:id");
                query.setParameter("id", row[0]);
                query.setParameter("version", row[1].toString());
                query.executeUpdate();
            }
        }
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = super.getDDLQueriesBefore();
        
        sql.add(getDDLRemoveColumn("SYSTEM_LOG", "PROCESS_DEFINITION_VERSION_"));

        sql.add(getDDLRemoveColumn("BPM_PROCESS_DEFINITION", "VERSION_"));
        sql.add(getDDLModifyColumnNullability("BPM_PROCESS_DEFINITION", "VERSION", dialect.getTypeName(Types.VARCHAR), false));

        sql.add(getDDLCreateUniqueKey("BPM_PROCESS_DEFINITION", "UK_DEFINITION_VERSION", "NAME", "VERSION"));
        return sql;
    }
}
