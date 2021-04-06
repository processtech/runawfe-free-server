package ru.runa.wfe.codegen;

import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.val;

class DbStructureAnalyzer {

    @RequiredArgsConstructor
    enum Type {
        BIGINT(false),
        BLOB(false),
        BOOLEAN(false),
        CHAR(true),
        DOUBLE(false),
        INT(false),
        TIMESTAMP(false),
        VARCHAR(true);

        public final boolean hasLength;
    }

    static class Column {
        String name;
        boolean isNotNull;
        Type type;
        Integer typeLength;
        boolean isPrimaryKey = false;
    }

    static class Table {
        String name;
        ArrayList<Column> columns = new ArrayList<>();
        HashMap<String, Column> columnsByName = new HashMap<>();
    }

    static class UniqueKey {
        String constraintName;
        Table table;
        ArrayList<Column> columns = new ArrayList<>();
    }

    static class ForeignKey {
        String constraintName;
        Table table;
        Column column;
        Table refTable;
        Column refColumn;
    }

    static class Index {
        String name;
        Table table;
        ArrayList<Column> columns = new ArrayList<>();
    }

    static class Migration {
        String name;
        Timestamp whenStarted;
        Timestamp whenFinished;
    }

    static class Structure {
        ArrayList<String> sequenceNames = new ArrayList<>();
        ArrayList<Table> tables = new ArrayList<>();
        HashMap<String, Table> tablesByName = new HashMap<>();
        ArrayList<UniqueKey> uniqueKeys = new ArrayList<>();
        ArrayList<ForeignKey> foreignKeys = new ArrayList<>();
        ArrayList<Index> indexes = new ArrayList<>();

        // Not a structure, but data controlling DbMigrationManager, so let's read DB in one place.
        ArrayList<Migration> migrations = new ArrayList<>();
        // TODO Remove in WFE 5:
        Integer version = null;
    }


    static Structure analyze(String jdbcUrl) throws Exception {
        Structure st = new Structure();

        Class.forName("org.postgresql.Driver");
        val conn = DriverManager.getConnection(jdbcUrl);

        // Read sequences.
        try (val stmt = conn.createStatement()) {
            val rs = stmt.executeQuery("select c.relname as name " +
                    "from pg_class c " +
                    "inner join pg_namespace n on (n.oid = c.relnamespace) " +
                    "where n.nspname = 'public' and c.relkind = 'S' " +
                    "order by c.relname");
            while (rs.next()) {
                st.sequenceNames.add(rs.getString(1));
            }
        }

        // Read tables.
        try (val stmt = conn.createStatement()) {
            val rs = stmt.executeQuery("select " +
                    "    c.relname as table_name, " +
                    "    a.attname as column_name, " +
                    "    a.attnotnull as not_null, " +
                    "    t.typname as type_name, " +
                    "    a.atttypmod as type_length " +
                    "from pg_namespace n " +
                    "inner join pg_class c on (c.relnamespace = n.oid and c.relkind = 'r') " +
                    "inner join pg_attribute a on (a.attrelid = c.oid and a.attnum > 0) " +
                    "inner join pg_type t on (t.oid = a.atttypid) " +
                    "where n.nspname = 'public' " +
                    "order by c.relname, a.attnum"
            );
            Table t = null;
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                if (t == null || !Objects.equals(t.name, tableName)) {
                    t = new Table();
                    t.name = tableName;
                    st.tables.add(t);
                    st.tablesByName.put(t.name, t);
                }
                val c = new Column();
                c.name = rs.getString("column_name");
                t.columns.add(c);
                t.columnsByName.put(c.name, c);
                c.isNotNull = rs.getBoolean("not_null");
                val typeName = rs.getString("type_name");
                switch (typeName) {
                    case "bool":
                        c.type = Type.BOOLEAN;
                        break;
                    case "bpchar":
                        c.type = Type.CHAR;
                        break;
                    case "float8":
                        c.type = Type.DOUBLE;
                        break;
                    case "int4":
                        c.type = Type.INT;
                        break;
                    case "int8":
                        c.type = Type.BIGINT;
                        break;
                    case "oid":
                        c.type = Type.BLOB;
                        break;
                    case "timestamp":
                        c.type = Type.TIMESTAMP;
                        break;
                    case "varchar":
                        c.type = Type.VARCHAR;
                        break;
                    default:
                        throw new Exception("Unsupported typeName \"" + typeName + "\" for column \"" + t.name + "." + c.name + "\"");
                }
                c.typeLength = rs.getInt("type_length");
                if (c.typeLength < 0) {
                    c.typeLength = null;
                    if (c.type.hasLength) {
                        throw new Exception("Missing typeLength for column \"" + t.name + "." + c.name + "\" of type \"" + c.type + "\"");
                    }
                } else if (c.typeLength <= 4) {
                    throw new Exception("Invalid typeLength " + c.typeLength + " for column \"" + t.name + "." + c.name + "\"");
                } else {
                    c.typeLength -= 4;
                    if (!c.type.hasLength) {
                        throw new Exception("Unexpected typeLength " + c.typeLength + " for column \"" + t.name + "." + c.name + "\" of type \"" +
                                c.type + "\"");
                    }
                }
            }
            rs.close();
        }

        // Read primary & unique keys.
        try (val stmt = conn.createStatement()) {
            val rs = stmt.executeQuery("select " +
                    "    co.contype as constraint_type, co.conname as constraint_name, c.relname as table_name, a.attname as column_name, " +
                    "    cok.column_idx " +
                    "from pg_namespace n " +
                    "inner join pg_constraint co on co.connamespace = n.oid and co.contype in ('p', 'u') " +
                    "inner join pg_class c on (c.oid = co.conrelid) " +
                    "left join /*lateral*/ unnest(co.conkey) with ordinality as cok(attnum, column_idx) on true " +
                    "left join pg_attribute a on (a.attrelid = c.oid and a.attnum = cok.attnum) " +
                    "where n.nspname = 'public' " +
                    "order by c.relname, co.conname, cok.column_idx"
            );
            UniqueKey uk = null;
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                String columnName = rs.getString("column_name");
                boolean isPk = Objects.equals(rs.getString("constraint_type"), "p");
                if (isPk) {
                    if (rs.getInt("column_idx") != 1) {
                        throw new Exception("Unsupported multi-column PK for table \"" + tableName + "\"");
                    }
                    st.tablesByName.get(tableName).columnsByName.get(columnName).isPrimaryKey = true;
                } else {
                    String constraintName = rs.getString("constraint_name");
                    if (uk == null || !Objects.equals(uk.constraintName, constraintName) || !Objects.equals(uk.table.name, tableName)) {
                        uk = new UniqueKey();
                        st.uniqueKeys.add(uk);
                        uk.constraintName = constraintName;
                        uk.table = st.tablesByName.get(tableName);
                    }
                    uk.columns.add(uk.table.columnsByName.get(columnName));
                }
            }
        }

        // Read foreign keys.
        try (val stmt = conn.createStatement()) {
            val rs = stmt.executeQuery("select " +
                    "    co.conname as constraint_name, c.relname as table_name, a.attname as column_name, cf.relname as ref_table_name, " +
                    "    af.attname as ref_column_name, cok.column_idx " +
                    "from pg_namespace n " +
                    "inner join pg_constraint co on (co.connamespace = n.oid and co.contype='f') " +
                    "inner join pg_class c on (c.oid = co.conrelid) " +
                    "inner join pg_class cf on (cf.oid = co.confrelid) " +
                    "inner join pg_namespace nf on (nf.oid = cf.relnamespace) " +
                    "left join unnest(co.conkey, co.confkey) with ordinality as cok(attnum, refattnum, column_idx) on true " +
                    "left join pg_attribute a on (a.attrelid = c.oid and a.attnum = cok.attnum) " +
                    "left join pg_attribute af on (af.attrelid = cf.oid and af.attnum = cok.refattnum) " +
                    "where n.nspname = 'public' and nf.nspname = 'public' " +
                    "order by c.relname, co.conname, column_idx\n"
            );
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                if (rs.getInt("column_idx") != 1) {
                    throw new Exception("Unsupported multi-column FK for table \"" + tableName + "\"");
                }
                val fk = new ForeignKey();
                st.foreignKeys.add(fk);
                fk.constraintName = rs.getString("constraint_name");
                fk.table = st.tablesByName.get(tableName);
                fk.column = fk.table.columnsByName.get(rs.getString("column_name"));
                fk.refTable = st.tablesByName.get(rs.getString("ref_table_name"));
                fk.refColumn = fk.refTable.columnsByName.get(rs.getString("ref_column_name"));
            }
        }

        // Read indexes.
        try (val stmt = conn.createStatement()) {
            val rs = stmt.executeQuery("select " +
                    "    ci.relname as index_name, ct.relname as table_name, pg_get_indexdef(i.indexrelid, column_idx + 1, true) as column_name " +
                    "from pg_index i " +
                    "inner join pg_class ci on (ci.oid = i.indexrelid) " +
                    "inner join pg_class ct on (ct.oid = i.indrelid) " +
                    "inner join pg_namespace n on (n.oid = ci.relnamespace) " +
                    "inner join pg_am am on (ci.relam = am.oid) " +
                    "left join generate_subscripts(i.indkey, 1) as column_idx on true " +
                    "where not i.indisprimary and not i.indisunique and n.nspname = 'public' " +
                    "order by ct.relname, ci.relname, column_idx;"
            );
            val regexColumnName = Pattern.compile("^[a-z][a-z0-9_]*$");
            Index i = null;
            while (rs.next()) {
                String indexName = rs.getString("index_name");
                String tableName = rs.getString("table_name");
                if (i == null || !Objects.equals(i.name, indexName) || !Objects.equals(i.table.name, tableName)) {
                    i = new Index();
                    st.indexes.add(i);
                    i.name = indexName;
                    i.table = st.tablesByName.get(tableName);
                }
                String columnName = rs.getString("column_name");
                if (!regexColumnName.matcher(columnName).matches()) {
                    throw new Exception("Unsupported column expression \"" + columnName + "\" in index \"" + indexName + "\" on table \"" +
                            tableName + "\"");
                }
                i.columns.add(i.table.columnsByName.get(columnName));
            }
        }

        // Read migrations.
        if (st.tablesByName.containsKey("db_migration")) {
            try (val stmt = conn.createStatement()) {
                val rs = stmt.executeQuery("select name, when_started, when_finished from db_migration order by when_started, name");
                while (rs.next()) {
                    val m = new Migration();
                    st.migrations.add(m);
                    m.name = rs.getString(1);
                    m.whenStarted = rs.getTimestamp(2);
                    m.whenFinished = rs.getTimestamp(3);
                }
            }
        }

        // Read DB version.
        boolean haveConstants = st.tablesByName.containsKey("wfe_constants");
        boolean haveConstant = st.tablesByName.containsKey("wfe_constant");
        if (haveConstants || haveConstant) {
            try (val stmt = conn.createStatement()) {
                String constantTableName = haveConstants ? "wfe_constants" : "wfe_constant";
                val rs = stmt.executeQuery("select value from " + constantTableName + " where name = 'ru.runa.database_version'");
                if (rs.next()) {
                    st.version = Integer.parseInt(rs.getString(1));
                }
            }
        }

        return st;
    }
}
