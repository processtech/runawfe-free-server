package ru.runa.wfe.codegen;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
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
        String name;
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

    static class Structure {
        ArrayList<Table> tables = new ArrayList<>();
        HashMap<String, Table> tablesByName = new HashMap<>();
        ArrayList<UniqueKey> uniqueKeys = new ArrayList<>();
        ArrayList<ForeignKey> foreignKeys = new ArrayList<>();
    }


    static Structure analyze(String jdbcUrl) throws Exception {
        Structure st = new Structure();

        Class.forName("org.postgresql.Driver");
        val conn = DriverManager.getConnection(jdbcUrl);

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
            val rs = stmt.executeQuery("select co.contype as constraint_type, co.conname as constraint_name, c.relname as table_name, a.attname as column_name, cok.column_idx " +
                    "from pg_namespace n " +
                    "inner join pg_constraint co on co.connamespace = n.oid and co.contype in ('p', 'u') " +
                    "inner join pg_class c on (c.oid = co.conrelid) " +
                    "left join /*lateral*/ unnest(co.conkey) with ordinality as cok(attnum, column_idx) on true " +
                    "left join pg_attribute a on (a.attrelid = c.oid and a.attnum = cok.attnum) " +
                    "where n.nspname = 'public' " +
                    "order by co.conname, c.relname, cok.column_idx"
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
                    if (uk == null || !Objects.equals(uk.name, constraintName) || !Objects.equals(uk.table.name, tableName)) {
                        uk = new UniqueKey();
                        st.uniqueKeys.add(uk);
                        uk.name = constraintName;
                        uk.table = st.tablesByName.get(tableName);
                    }
                    uk.columns.add(uk.table.columnsByName.get(columnName));
                }
            }
        }

        // Read foreign keys.
        try (val stmt = conn.createStatement()) {
            ForeignKey fk = null;
            // TODO ...
        }

        // Read indexes.
        // TODO ...

        return st;
    }
}
