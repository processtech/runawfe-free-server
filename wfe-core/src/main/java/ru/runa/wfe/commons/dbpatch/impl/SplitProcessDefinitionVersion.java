package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import ru.runa.wfe.commons.dbpatch.DbPatch;

public class SplitProcessDefinitionVersion extends DbPatch {

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
            // I want to avoid sequence setval(), since some SQL servers don't have sequences. So I rename table and secuence instead.
            add(getDDLRenameTable("bpm_process_definition", "bpm_process_definition_ver"));
            add(getDDLRenameSequence("seq_bpm_process_definition", "seq_bpm_process_definition_ver"));

            add(getDDLDropForeignKey("bpm_process", "fk_process_definition"));
            add(getDDLDropIndex("bpm_process", "ix_process_definition"));
            add(getDDLRenameColumn("bpm_process", "definition_id", new BigintColumnDef("definition_version_id", false)));
            add(getDDLCreateIndex("bpm_process", "ix_process_definition_ver", "definition_version_id"));
            add(getDDLCreateForeignKey("bpm_process", "fk_process_definition_ver", "definition_version_id", "bpm_definition_version", "id"));

            // Can add columns here, but not drop: first we must fill BPM_DEFINITION_VERSION table.
            add(getDDLCreateColumn("seq_bpm_process_definition_ver", new BigintColumnDef("definition_id", true)));
            add(getDDLCreateColumn("seq_bpm_process_definition_ver", new BigintColumnDef("subversion", true)));   // For future, will be 0 for now.

            // This new table will be filled from BPM_PROCESS_DEFINITION_VER using "group by name".
            add(getDDLCreateSequence("seq_bpm_process_definition"));
            add(getDDLCreateTable("bpm_process_definition",
                    new ArrayList<ColumnDef>() {{
                        add(new BigintColumnDef("id", false).setPrimaryKey());
                        add(new BigintColumnDef("latest_version_id", true));
                        add(new VarcharColumnDef("name", 1024, false));
                        add(new VarcharColumnDef("language", 4, false));
                        add(new VarcharColumnDef("description", 1024));
                        add(new VarcharColumnDef("category", 1024, false));
                    }},
                    "(name)"
            ));
        }};
    }

    @Override
    public void executeDML(Connection conn) throws Exception {
        try (val stmt = conn.createStatement()) {

            // Fill new BPM_PROCESS_DEFINITION table (except latest_version_id).
            String idName, idValue;
            switch (dbType) {
                case ORACLE:
                    idName = "id, ";
                    idValue = "seq_bpm_process_definition.nextval, ";
                    break;
                case POSTGRESQL:
                    idName = "id, ";
                    idValue = "nextval('seq_bpm_process_definition'), ";
                    break;
                default:
                    idName = "";
                    idValue = "";
            }
            stmt.executeUpdate("insert into bpm_process_definition (" + idName + "name, language, description, category) " +
                    // Take distinct name and arbitrary values for other fields; max() is as good as anything else.
                    "select " + idValue + "name, max(language), max(description), max(category) " +
                    "from bpm_process_definition " +
                    "group by name"
            );

            // Fill PROCESS_DEFINITION_VER.DEFINITION_ID (after we filled PROCESS_DEFINITION table).
            stmt.executeUpdate("update bpm_process_definition_version set " +
                    "definition_id = (select max(id) from bpm_process_definition where bpm_process_definition.name = bpm_process_definition_ver.name), " +
                    "subversion = 0"
            );

            // Fill PROCESS_DEFINITION.LATEST_VERSION_ID (after we filled PROCESS_DEFINITION_VER.DEFINITION_ID).
            stmt.executeUpdate("update bpm_process_definition set latest_version_id = " +
                    "(select max(id) from bpm_process_definition where bpm_process_definition.id = bpm_process_definition_ver.definition_id)"
            );

            // Fix permissions.
            try (
                    val update = conn.prepareStatement("update permission_mapping " +
                            "set object_type='DEFINITION2', object_id=? " +
                            "where object_type='DEFINITION' and object_id=?");
                    val select = conn.createStatement()
            ) {
                ResultSet rs = select.executeQuery("select name, id from bpm_process_definition");
                while (rs.next()) {
                    update.setLong(1, rs.getString(1).hashCode());
                    update.setLong(2, rs.getLong(2));
                    update.executeUpdate();
                }
            }
            stmt.executeUpdate("update permission_mapping set object_type='DEFINITION' where object_type='DEFINITION2'");
        }
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        return new ArrayList<String>() {{
            add(getDDLDropColumn("bpm_process_definition_ver", "name"));
            add(getDDLDropColumn("bpm_process_definition_ver", "language"));
            add(getDDLDropColumn("bpm_process_definition_ver", "description"));
            add(getDDLDropColumn("bpm_process_definition_ver", "category"));
            add(getDDLModifyColumnNullability("seq_bpm_process_definition_ver", "definition_id", dialect.getTypeName(Types.BIGINT), false));
            add(getDDLModifyColumnNullability("seq_bpm_process_definition_ver", "subversion", dialect.getTypeName(Types.BIGINT), false));

            add(getDDLCreateUniqueKey("bpm_process_definition_ver", "fx_version_definition_ver", "definition_id", "version"));
            add(getDDLCreateForeignKey("bpm_process_definition_ver", "fk_version_definition", "definition_id", "bpm_process_definition", "id"));

            add(getDDLCreateIndex("bpm_process_definition", "ix_definition_latest_ver", "latest_version_id"));
        }};
    }
}
