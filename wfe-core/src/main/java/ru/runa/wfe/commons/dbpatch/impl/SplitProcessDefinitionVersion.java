package ru.runa.wfe.commons.dbpatch.impl;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbpatch.DBPatch;

public class SplitProcessDefinitionVersion extends DBPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {

        // Table BPM_PROCESS_DEFINITION does not have UK(name, version), so perform sanity check first.
        @SuppressWarnings("unchecked")
        List<Object[]> duplicates = sessionFactory.getCurrentSession()
                .createSQLQuery("select name, version, count(*) from bpm_process_definition group by name, version having count(*) > 1")
                .list();
        if (!duplicates.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Duplicated (name,version): ");
            boolean first = true;
            for (Object[] row : duplicates) {
                String name = (String) row[0];
                Long version = (Long) row[1];
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append("(").append(name).append(",").append(version).append(")");
            }
            throw new RuntimeException(sb.toString());
        }

        return new ArrayList<String>() {{
            // Create new table because will need to fill it from BPM_PROCESS_DEFINITION using "group by name".
            add(getDDLCreateSequence("seq_bpm_definition"));
            add(getDDLCreateTable("bpm_definition",
                    new ArrayList<ColumnDef>() {{
                        add(new BigintColumnDef("id", false).setPrimaryKey());
                        add(new VarcharColumnDef("name", 1024, false));
                        add(new VarcharColumnDef("language", 4, false));
                        add(new VarcharColumnDef("description", 1024));
                        add(new VarcharColumnDef("category", 1024, false));
                    }},
                    "(name)"
            ));

            // This one will use old SEQ_BPM_PROCESS_DEFINITION sequence,
            // since H2 cannot rename sequence, and I'm too lazy to add DDL for setting sequence value.
            add(getDDLCreateTable("bpm_definition_version",
                    new ArrayList<ColumnDef>() {{
                        add(new BigintColumnDef("id", false).setPrimaryKey());
                        add(new BigintColumnDef("definition_id", false));
                        add(new BigintColumnDef("version", false));
                        add(new BigintColumnDef("subversion", false));   // Added for future ticket, will be 0 for now.
                        add(new BlobColumnDef("bytes"));
                        add(new TimestampColumnDef("create_date", false));
                        add(new BigintColumnDef("create_user_id"));
                        add(new TimestampColumnDef("update_date"));
                        add(new BigintColumnDef("update_user_id"));
                        add(new TimestampColumnDef("subprocess_binding_date"));
                    }},
                    "(definition_id, version, subversion)"
            ));
        }};
    }

    @Override
    public void executeDML(Session session) {
        // New BPM_DEFINITION table will be generated with fresh IDs.
        {
            String idName, idValue;
            switch (dbType) {
                case ORACLE:
                    idName = "id, ";
                    idValue = "seq_bpm_definition.nextval, ";
                    break;
                case POSTGRESQL:
                    idName = "id, ";
                    idValue = "nextval('seq_bpm_definition'), ";
                    break;
                default:
                    idName = "";
                    idValue = "";
            }
            session.createSQLQuery(
                    "insert into bpm_definition (" + idName + "name, language, description, category) " +
                            // Take distinct name and arbitrary values for other fields; max() is as good as anything else.
                            "select " + idValue + "name, max(language), max(description), max(category) " +
                            "from bpm_process_definition " +
                            "group by name"
            ).executeUpdate();
        }

        // New BPM_DEFINITION_VERSION table will inherit old BPM_PROCESS_DEFINITION table's ids.
        session.createSQLQuery(
                "insert into bpm_definition_version (id, definition_id, version, bytes, create_date, create_user_id, update_date, update_user_id, subprocess_binding_date) " +
                        "select pd.id, d.id, pd.version, pd.bytes, pd.create_date, pd.create_user_id, pd.update_date, pd.update_user_id, pd.subprocess_binding_date " +
                        "from bpm_process_definition pd " +
                        "inner join bpm_definition d on (d.name = pd.name)"
        ).executeUpdate();

        {
            // Update PERMISSION_MAPPING.OBJECT_ID.
            // Temporarily replace OBJECT_TYPE with fake one, to avoid mistaking new OBJECT_ID with old name hash.
            SQLQuery q = session.createSQLQuery("update permission_mapping " +
                    "set object_type='DEFINITION2', object_id=:oldId " +
                    "where object_type='DEFINITION' and object_id=:newId");

            @SuppressWarnings("unchecked") List<Object[]> rows = session.createSQLQuery("select id, name from bpm_definition").list();
            for (Object[] row : rows) {
                q.setParameter("oldId", row[1].hashCode());
                q.setParameter("newId", row[0]);
                q.executeUpdate();
            }

            session.createSQLQuery("update permission_mapping set object_type='DEFINITION' where object_type='DEFINITION2'").executeUpdate();
        }
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        return new ArrayList<String>() {{
            // Do it before BPM_PROCESS_DEFINITION is dropped.
            // Now column BPM_PROCESS.DEFINITION_ID will reference definition version, so rename it, and recreate FK and index on it.
            // I'll try to do without creating another DEFINITION_ID column to avoid connection timeout, since BPM_PROCESS table may be huge.
            add(getDDLDropForeignKey("bpm_process", "fk_process_definition"));
            add(getDDLDropIndex("bpm_process", "ix_process_definition"));
            add(getDDLRenameColumn("bpm_process", "definition_id", new BigintColumnDef("definition_version_id", false)));
            add(getDDLCreateForeignKey("bpm_process", "fk_process_definition_version", "definition_version_id", "bpm_definition_version", "id"));
            add(getDDLCreateIndex("bpm_process", "ix_process_definition_version", "definition_version_id"));

            // For speed, do it after data is populated.
            add(getDDLCreateForeignKey("bpm_definition_version", "fk_version_definition", "definition_id", "bpm_definition", "id"));
            add(getDDLCreateIndex("bpm_definition_version", "ix_version_definition", "definition_id"));
            add(getDDLCreateForeignKey("bpm_definition_version", "fk_definition_create_user", "create_user_id", "executor", "id"));
            add(getDDLCreateForeignKey("bpm_definition_version", "fk_definition_update_user", "update_user_id", "executor", "id"));

            // TODO First, scan all usages of BPM_PROCESS_DEFINITION in TNMS, replace with BPM_DEFINITION and BPM_DEFINITION_VERSION.
            //      Then drop this old table manually.
//            add(getDDLDropTable("bpm_process_definition"));
        }};
    }
}
