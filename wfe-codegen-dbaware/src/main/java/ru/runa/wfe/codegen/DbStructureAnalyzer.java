package ru.runa.wfe.codegen;

import java.sql.DriverManager;
import java.util.ArrayList;
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
    }

    static class Table {
        String name;
        ArrayList<Column> columns = new ArrayList<>();
    }

    static class PrimaryKey {
        String tableName;
        String columnName;
    }

    static class FlatForeignKey {
        String constraintName;
        Table table;
        Column column;
        Table refTable;
        Column refColumn;
    }

    static class Structure {
        ArrayList<Table> tables = new ArrayList<>();
    }

    static Structure analyze(String jdbcUrl) throws Exception {
        Structure st = new Structure();

        Class.forName("org.postgresql.Driver");
        val conn = DriverManager.getConnection(jdbcUrl);
        val stmt = conn.createStatement();

        Table t = null;
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
        while (rs.next()) {
            String tableName = rs.getString("table_name");
            if (t == null || !Objects.equals(t.name, tableName)) {
                t = new Table();
                st.tables.add(t);
                t.name = tableName;
            }
            val c = new Column();
            t.columns.add(c);
            c.name = rs.getString("column_name");
            c.isNotNull = rs.getBoolean("not_null");
            val typeName = rs.getString("type_name");
            switch (typeName) {
                case "bool":      c.type = Type.BOOLEAN; break;
                case "bpchar":    c.type = Type.CHAR; break;
                case "float8":    c.type = Type.DOUBLE; break;
                case "int4":      c.type = Type.INT; break;
                case "int8":      c.type = Type.BIGINT; break;
                case "oid":       c.type = Type.BLOB; break;
                case "timestamp": c.type = Type.TIMESTAMP; break;
                case "varchar":   c.type = Type.VARCHAR; break;
                default: throw new Exception("Unsupported typeName \"" + typeName + "\" for column \"" + t.name + "." + c.name + "\"");
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

        return st;
    }
}
