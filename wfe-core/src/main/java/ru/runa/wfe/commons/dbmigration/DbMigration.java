package ru.runa.wfe.commons.dbmigration;

import com.google.common.base.Joiner;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DbType;

/**
 * Base class for database migration (which are applied at startup).
 * 
 * @author Dofs
 * @see DbMigrationManager
 */
@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class DbMigration {
    protected final Log log = LogFactory.getLog(getClass());
    protected final Dialect dialect = ApplicationContextFactory.getDialect();
    protected final DbType dbType = ApplicationContextFactory.getDbType();

    private ThreadLocal<String> currentCategory = new ThreadLocal<>();
    private ThreadLocal<Session> currentSession = new ThreadLocal<>();

    @Autowired
    protected SessionFactory sessionFactory;

    public void execute() throws Exception {
        try {
            Session session = sessionFactory.getCurrentSession();
            currentSession.set(session);

            currentCategory.set("[DDLBefore]");
            executeDDLBefore();

            currentCategory.set(null);
            session.setCacheMode(CacheMode.IGNORE);
            executeDML(session);
            session.flush();

            currentCategory.set("[DDLAfter]");
            executeDDLAfter();
        } finally {
            currentCategory.set(null);
            currentSession.set(null);
        }
    }

    protected void executeDDLBefore() {
    }

    /**
     * Execute migration's DML statements in one transaction.
     * 
     * It's allowed to use only raw SQL because hibernate mappings could not work in old DB version.
     * 
     * @deprecated Use pure JDBC, by overriding {@link #executeDML(Connection)}.
     */
    @Deprecated
    public void executeDML(Session session) throws Exception {
        executeDML(session.connection());
    }

    public void executeDML(Connection conn) throws Exception {
    }

    protected void executeDDLAfter() {
    }

    /**
     * Helper for subclasses.
     * <p>
     * ImmutableList.of() requires all list items to be non-null, but getDDLCreateSequence() may return null.
     * Arrays.asList() does not support add() and addAll() operations.
     */
    @SafeVarargs
    protected final <T> List<T> list(T... oo) {
        if (oo.length == 0) {
            return Collections.emptyList();
        } else if (oo.length == 1) {
            return Collections.singletonList(oo[0]);
        } else {
            val result = new ArrayList<T>(oo.length);
            Collections.addAll(result, oo);
            return result;
        }
    }

    /**
     * Helper for subclasses, to insert rows into tables with auto-incremented PK. Use in columns list.
     */
    protected final String insertPkColumn() {
        switch (dbType) {
            case ORACLE:
            case POSTGRESQL:
                return "id, ";
            default:
                return "";
        }
    }

    /**
     * Helper for subclasses, to insert rows into tables with auto-incremented PK. Use in values() list.
     */
    protected final String insertPkNextVal(String tableName) {
        switch (dbType) {
            case ORACLE:
                return "seq_" + tableName + ".nextval, ";
            case POSTGRESQL:
                return "nextval('seq_" + tableName + "'), ";
            default:
                return "";
        }
    }

    private int executeOneUpdate(Statement stmt, String category, String query, int lastResult) throws SQLException {
        if (StringUtils.isBlank(query)) {
            return lastResult;
        }
        if (category != null) {
            log.info(category + ": " + query);
        }
        return stmt.executeUpdate(query);
    }

    /**
     * Helper for subclasses, for executeDML() method.
     *
     * @return Result of last update.
     */
    protected final int executeUpdates(String... queries) {
        try {
            val conn = currentSession.get().connection();
            val category = currentCategory.get();
            try (val stmt = conn.createStatement()) {
                int result = 0;
                for (val q : queries) {
                    result = executeOneUpdate(stmt, category, q, result);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper for subclasses, for executeDDL...() methods.
     *
     * @return Result of last update.
     */
    @SafeVarargs
    protected final int executeUpdates(List<String>... queries) {
        try {
            val conn = currentSession.get().connection();
            val category = currentCategory.get();
            try (val stmt = conn.createStatement()) {
                int result = 0;
                for (val qq : queries) {
                    if (qq != null) {
                        for (val q : qq) {
                            result = executeOneUpdate(stmt, category, q, result);
                        }
                    }
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkIndentifierLength(String id) {
        if (id != null && id.length() > 30) {
            throw new RuntimeException("Identifier \"" + id + "\".length " + id.length() + " > 30 (Oracle restriction)");
        }
    }

    protected final List<String> getDDLCreateSequence(String sequenceName) {
        checkIndentifierLength(sequenceName);
        switch (dbType) {
            case ORACLE:
            case POSTGRESQL:
                return list("create sequence " + sequenceName);
            default:
                return null;
        }
    }

    protected final List<String> getDDLCreateSequence(String sequenceName, long nextValue) {
        checkIndentifierLength(sequenceName);
        switch (dbType) {
            case ORACLE:
            case POSTGRESQL:
                return list("create sequence " + sequenceName + " start with " + nextValue);
            default:
                return null;
        }
    }

    protected final List<String> getDDLDropSequence(String sequenceName) {
        switch (dbType) {
            case ORACLE:
            case POSTGRESQL:
                return list("drop sequence " + sequenceName);
            default:
                return null;
        }
    }

    protected final List<String> getDDLRenameSequence(String sequenceName, String newName) {
        checkIndentifierLength(newName);
        switch (dbType) {
            case ORACLE:
                return list("rename " + sequenceName + " to " + newName);
            case POSTGRESQL:
                return list("alter sequence " + sequenceName + " rename to " + newName);
            default:
                return null;
        }
    }

    protected final List<String> getDDLCreateTable(String tableName, List<ColumnDef> columnDefinitions) {
        val query = new StringBuilder();
        query.append("CREATE TABLE ").append(tableName).append(" (");
        for (ColumnDef columnDef : columnDefinitions) {
            if (columnDefinitions.indexOf(columnDef) > 0) {
                query.append(", ");
            }
            query.append(columnDef.name).append(" ").append(columnDef.getSqlTypeName(dialect));
            if (columnDef.primaryKey) {
                // TODO Different SQL servers will generate different PK constraint name.
                //      Instead, should generate "pk_table_name" and (optionally) "seq_table_name" in separate statement
                //      (for that getDDL...() methods now return List<String>; no separate getDDLCreateSequence() should be needed)
                //      and warn+trim or fail if checkIdentifierLength("pk_table_name") fails.
                if (columnDef.autoIncremented) {
                    checkIndentifierLength("seq_" + tableName);
                } else {
                    checkIndentifierLength("pk_" + tableName);
                }

                String primaryKeyModifier;
                switch (dbType) {
                case HSQL:
                case MSSQL:
                    if (columnDef.autoIncremented) {
                        primaryKeyModifier = "IDENTITY NOT NULL PRIMARY KEY";
                    } else {
                        primaryKeyModifier = "NOT NULL PRIMARY KEY";
                    }
                    break;
                case ORACLE:
                    primaryKeyModifier = "NOT NULL PRIMARY KEY";
                    break;
                case POSTGRESQL:
                    primaryKeyModifier = "PRIMARY KEY";
                    break;
                case MYSQL:
                    if (columnDef.autoIncremented) {
                        primaryKeyModifier = "NOT NULL PRIMARY KEY AUTO_INCREMENT";
                    } else {
                        primaryKeyModifier = "NOT NULL PRIMARY KEY";
                    }
                    break;
                case H2:
                    if (columnDef.autoIncremented) {
                        primaryKeyModifier = "GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY";
                    } else {
                        primaryKeyModifier = "NOT NULL PRIMARY KEY";
                    }
                    break;
                default:
                    primaryKeyModifier = "PRIMARY KEY";
                    break;
                }
                query.append(" ").append(primaryKeyModifier);
                continue;
            }
            if (!columnDef.allowNulls) {
                query.append(" NOT NULL");
            }
        }
        query.append(")");
        return list(query.toString());
    }

    protected final List<String> getDDLRenameTable(String oldTableName, String newTableName) {
        checkIndentifierLength(newTableName);
        switch (dbType) {
            case MSSQL:
                return list("sp_rename '" + oldTableName + "', '" + newTableName + "'");
            case MYSQL:
                return list("RENAME TABLE " + oldTableName + " TO " + newTableName);
            default:
                return list("ALTER TABLE " + oldTableName + " RENAME TO " + newTableName);
        }
    }

    protected final List<String> getDDLDropTable(String tableName) {
        return list("DROP TABLE " + tableName);
    }

    protected final List<String> getDDLCreateIndex(String tableName, String indexName, String... columnNames) {
        checkIndentifierLength(indexName);
        for (val cn : columnNames) {
            checkIndentifierLength(cn);
        }
        String conjunctedColumnNames = Joiner.on(", ").join(columnNames);
        return list("CREATE INDEX " + indexName + " ON " + tableName + " (" + conjunctedColumnNames + ")");
    }

    protected final List<String> getDDLCreateUniqueKey(String tableName, String constraintName, String... columnNames) {
        checkIndentifierLength(constraintName);
        for (val cn : columnNames) {
            checkIndentifierLength(cn);
        }
        String conjunctedColumnNames = Joiner.on(", ").join(columnNames);
        return list("ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName + " UNIQUE (" + conjunctedColumnNames + ")");
    }

    protected final List<String> getDDLRenameIndex(String tableName, String indexName, String newIndexName) {
        checkIndentifierLength(newIndexName);
        switch (dbType) {
            case MSSQL:
                return list("sp_rename '" + tableName + "." + indexName + "', '" + newIndexName + "'");
            case H2:
            case ORACLE:
            case POSTGRESQL:
                return list("alter index " + indexName + " rename to " + newIndexName);
            default:
                throw new NotImplementedException();  // TODO ...
        }
    }

    protected final List<String> getDDLDropIndex(String tableName, String indexName) {
        switch (dbType) {
            case H2:
            case ORACLE:
            case POSTGRESQL:
                return list("DROP INDEX " + indexName);
            default:
                return list("DROP INDEX " + indexName + " ON " + tableName);
        }
    }

    protected final List<String> getDDLCreateForeignKey(String tableName, String keyName, String columnName, String refTableName, String refColumnName) {
        checkIndentifierLength(keyName);
        return list("ALTER TABLE " + tableName + " ADD CONSTRAINT " + keyName + " FOREIGN KEY (" + columnName + ") REFERENCES " + refTableName +
                " (" + refColumnName + ")");
    }

    protected final List<String> getDDLCreatePrimaryKey(String tableName, String keyName, String columnName) {
        checkIndentifierLength(keyName);
        return list("ALTER TABLE " + tableName + " ADD CONSTRAINT " + keyName + " PRIMARY KEY (" + columnName + ")");
    }

    protected final List<String> getDDLRenameForeignKey(String keyName, String newKeyName) {
        checkIndentifierLength(newKeyName);
        switch (dbType) {
            case MSSQL:
                return list("sp_rename '" + keyName + "', '" + newKeyName + "'");
            default:
                throw new NotImplementedException();  // TODO ...
        }
    }

    protected final List<String> getDDLDropForeignKey(String tableName, String keyName) {
        String constraint;
        switch (dbType) {
            case MYSQL:
                constraint = "FOREIGN KEY";
                break;
            default:
                constraint = "CONSTRAINT";
                break;
        }
        return list("ALTER TABLE " + tableName + " DROP " + constraint + " " + keyName);
    }

    protected final List<String> getDDLCreateColumn(String tableName, ColumnDef columnDef) {
        String lBraced = "";
        String rBraced = "";
        if (dbType == DbType.ORACLE) {
            lBraced = "(";
            rBraced = ")";
        }
        val query = new StringBuilder();
        query.append("ALTER TABLE ").append(tableName).append(" ADD ").append(lBraced);
        query.append(columnDef.name).append(" ").append(columnDef.getSqlTypeName(dialect));
        if (columnDef.defaultValue != null) {
            query.append(" DEFAULT ").append(columnDef.defaultValue);
        }
        if (!columnDef.allowNulls) {
            query.append(" NOT NULL");
        }
        query.append(rBraced);
        return list(query.toString());
    }

    protected final List<String> getDDLRenameColumn(String tableName, String oldColumnName, ColumnDef newColumnDef) {
        switch (dbType) {
            case ORACLE:
            case POSTGRESQL:
                return list("ALTER TABLE " + tableName + " RENAME COLUMN " + oldColumnName + " TO " + newColumnDef.name);
            case MSSQL:
                return list("sp_rename '" + tableName + "." + oldColumnName + "', '" + newColumnDef.name + "', 'COLUMN'");
            case MYSQL:
                return list("ALTER TABLE " + tableName + " CHANGE " + oldColumnName + " " + newColumnDef.name + " " + newColumnDef.getSqlTypeName(dialect));
            default:
                return list("ALTER TABLE " + tableName + " ALTER COLUMN " + oldColumnName + " RENAME TO " + newColumnDef.name);
        }
    }

    protected final List<String> getDDLModifyColumn(String tableName, String columnName, String sqlTypeName) {
        switch (dbType) {
            case ORACLE:
                return list("ALTER TABLE " + tableName + " MODIFY(" + columnName + " " + sqlTypeName + ")");
            case POSTGRESQL:
                return list("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TYPE " + sqlTypeName);
            case MYSQL:
                return list("ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + sqlTypeName);
            default:
                return list("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + sqlTypeName);
        }
    }

    protected final List<String> getDDLModifyColumnNullability(String tableName, String columnName, String currentSqlTypeName,
            @SuppressWarnings("SameParameterValue") boolean nullable) {
        switch (dbType) {
            case H2:
            case HSQL:
                return list("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " SET " + (nullable ? "NULL" : "NOT NULL"));
            case MYSQL:
                return list("ALTER TABLE " + tableName + " MODIFY " + columnName + " " + currentSqlTypeName + " " + (nullable ? "NULL" : "NOT NULL"));
            case ORACLE:
                return list("ALTER TABLE " + tableName + " MODIFY(" + columnName + " " + (nullable ? "NULL" : "NOT NULL") + ")");
            case POSTGRESQL:
                return list("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + (nullable ? "DROP" : "SET") + " NOT NULL");
            default:
                return list("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + currentSqlTypeName + " " + (nullable ? "NULL" : "NOT NULL"));
        }
    }

    protected final List<String> getDDLDropColumn(String tableName, String columnName) {
        return list("ALTER TABLE " + tableName + " DROP COLUMN " + columnName);
    }

    protected final List<String> getDDLTruncateTable(String tableName) {
        return list("TRUNCATE TABLE " + tableName);
    }

    protected final List<String> getDDLTruncateTableUsingDelete(@SuppressWarnings("SameParameterValue") String tableName) {
        return list("DELETE FROM " + tableName);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static class ColumnDef {
        private boolean primaryKey;
        private boolean autoIncremented;
        private final String name;
        private int sqlType;
        private String sqlTypeName;
        private final boolean allowNulls;
        private String defaultValue;

        /**
         * @deprecated Use shortcut subclasses (BigintColumnDef, etc.); create missing subclasses. Finally, DELETE this constructor.
         */
        @Deprecated
        public ColumnDef(String name, int sqlType, boolean allowNulls) {
            checkIndentifierLength(name);
            this.name = name;
            this.sqlType = sqlType;
            this.allowNulls = allowNulls;
        }

        /**
         * @deprecated Use shortcut subclasses; create missing subclasses. Finally, make this constructor protected.
         */
        @Deprecated
        public ColumnDef(String name, String sqlTypeName, boolean allowNulls) {
            checkIndentifierLength(name);
            this.name = name;
            this.sqlTypeName = sqlTypeName;
            this.allowNulls = allowNulls;
        }

        /**
         * Creates column def which allows null values.
         *
         * @deprecated Use shortcut subclasses; create missing subclasses. Finally, delete this constructor.
         */
        @Deprecated
        public ColumnDef(String name, int sqlType) {
            this(name, sqlType, true);
        }

        /**
         * Creates column def which allows null values.
         *
         * @deprecated Use shortcut subclasses; create missing subclasses. Finally, delete this constructor.
         */
        @Deprecated
        public ColumnDef(String name, String sqlTypeName) {
            this(name, sqlTypeName, true);
        }

        public String getSqlTypeName(Dialect dialect) {
            if (sqlTypeName != null) {
                return sqlTypeName;
            }
            return dialect.getTypeName(sqlType);
        }

        public ColumnDef setPrimaryKey() {
            primaryKey = true;
            autoIncremented = true;
            return this;
        }

        public ColumnDef setPrimaryKeyNoAutoInc() {
            primaryKey = true;
            return this;
        }

        public ColumnDef setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
    }

    @SuppressWarnings({"WeakerAccess", "deprecation"})
    public class BigintColumnDef extends ColumnDef {
        public BigintColumnDef(String name, boolean allowNulls) {
            super(name, Types.BIGINT, allowNulls);
        }
        public BigintColumnDef(String name) {
            this(name, true);
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
    public class BlobColumnDef extends ColumnDef {
        public BlobColumnDef(String name, boolean allowNulls) {
            super(name, dialect.getTypeName(Types.BLOB), allowNulls);
        }
        public BlobColumnDef(String name) {
            this(name, true);
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
    public class BooleanColumnDef extends ColumnDef {
        public BooleanColumnDef(String name, boolean allowNulls) {
            super(name, dialect.getTypeName(Types.BIT), allowNulls);
        }
        public BooleanColumnDef(String name) {
            this(name, true);
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
    public class CharColumnDef extends ColumnDef {
        public CharColumnDef(String name, int length, boolean allowNulls) {
            super(name, dialect.getTypeName(Types.CHAR, length, length, length), allowNulls);
        }
        public CharColumnDef(String name, int length) {
            this(name, length, true);
        }
    }

    /**
     * @deprecated Use TimestampColumnDef: I believe it's effectively the same but more clear.
     */
    @Deprecated
    @SuppressWarnings({"unused", "WeakerAccess"})
    public class DateColumnDef extends ColumnDef {
        public DateColumnDef(String name, boolean allowNulls) {
            super(name, dialect.getTypeName(Types.DATE), allowNulls);
        }
        public DateColumnDef(String name) {
            this(name, true);
        }
    }


    @SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
    public class DoubleColumnDef extends ColumnDef {
        public DoubleColumnDef(String name, boolean allowNulls) {
            super(name, dialect.getTypeName(Types.DOUBLE), allowNulls);
        }
        public DoubleColumnDef(String name) {
            this(name, true);
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
    public class IntColumnDef extends ColumnDef {
        public IntColumnDef(String name, boolean allowNulls) {
            super(name, dialect.getTypeName(Types.INTEGER), allowNulls);
        }
        public IntColumnDef(String name) {
            this(name, true);
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
    public class TimestampColumnDef extends ColumnDef {
        public TimestampColumnDef(String name, boolean allowNulls) {
            super(name, dialect.getTypeName(Types.TIMESTAMP), allowNulls);
        }
        public TimestampColumnDef(String name) {
            this(name, true);
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
    public class VarcharColumnDef extends ColumnDef {
        public VarcharColumnDef(String name, int length, boolean allowNulls) {
            super(name, dialect.getTypeName(Types.VARCHAR, length, length, length), allowNulls);
        }
        public VarcharColumnDef(String name, int length) {
            this(name, length, true);
        }
    }
}