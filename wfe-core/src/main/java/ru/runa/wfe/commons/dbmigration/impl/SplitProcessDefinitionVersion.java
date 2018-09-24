package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class SplitProcessDefinitionVersion extends DbMigration {

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void executeDDLBefore() {

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

        executeUpdates(
                // I want to avoid sequence setval(), since some SQL servers don't have sequences. So I rename table and secuence instead.
                getDDLRenameTable("bpm_process_definition", "bpm_process_definition_ver"),
                getDDLRenameSequence("seq_bpm_process_definition", "seq_bpm_process_definition_ver"),

                getDDLDropForeignKey("bpm_process", "fk_process_definition"),
                getDDLDropIndex("bpm_process", "ix_process_definition"),
                getDDLRenameColumn("bpm_process", "definition_id", new BigintColumnDef("definition_version_id", false)),
                getDDLCreateIndex("bpm_process", "ix_process_definition_ver", "definition_version_id"),
                getDDLCreateForeignKey("bpm_process", "fk_process_definition_ver", "definition_version_id", "bpm_process_definition_ver", "id"),

                // Can add columns here, but not drop: first we must fill BPM_DEFINITION_VERSION table.
                getDDLCreateColumn("bpm_process_definition_ver", new BigintColumnDef("definition_id", true)),
                getDDLCreateColumn("bpm_process_definition_ver", new BigintColumnDef("subversion", true)),   // For future, will be 0 for now.

                // This new table will be filled from BPM_PROCESS_DEFINITION_VER using "group by name".
                getDDLCreateSequence("seq_bpm_process_definition"),
                getDDLCreateTable("bpm_process_definition",
                        new ArrayList<ColumnDef>() {{
                            add(new BigintColumnDef("id", false).setPrimaryKey());
                            add(new BigintColumnDef("latest_version_id", true));
                            add(new VarcharColumnDef("name", 1024, false));
                            add(new VarcharColumnDef("language", 10, false));
                            add(new VarcharColumnDef("description", 1024));
                            add(new VarcharColumnDef("category", 1024, false));
                        }}
                ),
                getDDLCreateUniqueKey("bpm_process_definition", "uk_process_definition_name", "name")
        );
    }

    @Override
    public void executeDML(Connection conn) throws Exception {
        try (val stmt = conn.createStatement()) {

            // Fill new BPM_PROCESS_DEFINITION table (except latest_version_id).
            stmt.executeUpdate("insert into bpm_process_definition (" + insertPkColumn() + "name, language, description, category) " +
                    // Take distinct name and arbitrary values for other fields (they all should be the same in all rows with same name);
                    // max() is as good as anything else.
                    "select " + insertPkNextVal("bpm_process_definition") + "d.name, d.language, d.description, d.category " +
                    "from (" +
                    "    select name, max(language) as language, max(description) as description, max(category) as category " +
                    "    from bpm_process_definition_ver " +
                    "    group by name" +
                    ") d"
            );

            // Fill PROCESS_DEFINITION_VER.DEFINITION_ID (after we filled PROCESS_DEFINITION table).
            stmt.executeUpdate("update bpm_process_definition_ver set " +
                    "definition_id = (select max(id) from bpm_process_definition where bpm_process_definition.name = bpm_process_definition_ver.name), " +
                    "subversion = 0"
            );

            // Fill PROCESS_DEFINITION.LATEST_VERSION_ID (after we filled PROCESS_DEFINITION_VER.DEFINITION_ID).
            stmt.executeUpdate("update bpm_process_definition set latest_version_id = " +
                    "(select max(id) from bpm_process_definition_ver where bpm_process_definition.id = bpm_process_definition_ver.definition_id)"
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
    protected void executeDDLAfter() {
        executeUpdates(
                getDDLDropColumn("bpm_process_definition_ver", "name"),
                getDDLDropColumn("bpm_process_definition_ver", "language"),
                getDDLDropColumn("bpm_process_definition_ver", "description"),
                getDDLDropColumn("bpm_process_definition_ver", "category"),
                getDDLModifyColumnNullability("bpm_process_definition_ver", "definition_id", dialect.getTypeName(Types.BIGINT), false),
                getDDLModifyColumnNullability("bpm_process_definition_ver", "subversion", dialect.getTypeName(Types.BIGINT), false),

                getDDLCreateUniqueKey("bpm_process_definition_ver", "uk_version_definition_ver", "definition_id", "version"),
                getDDLCreateForeignKey("bpm_process_definition_ver", "fk_version_definition", "definition_id", "bpm_process_definition", "id"),

                getDDLCreateIndex("bpm_process_definition", "ix_definition_latest_ver", "latest_version_id")
        );
    }
}
