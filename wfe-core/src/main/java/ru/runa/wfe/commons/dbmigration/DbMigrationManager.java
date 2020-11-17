package ru.runa.wfe.commons.dbmigration;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.val;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ManualTransactionManager;
import ru.runa.wfe.commons.ManualTransactionManager.TxRunnable;
import ru.runa.wfe.commons.dbmigration.impl.RemoveWfeConstants;

@Component
@CommonsLog
public class DbMigrationManager {

    /**
     * Instance of this class is passed around between checkDbInitialized() and runAll() calls,
     * to avoid querying DB_MIGRATION table and old "version" twice, if possible.
     */
    public static class Context {
        private HashSet<String> appliedMigrationNames = null;
        private Integer oldDbVersion = null;

        public boolean isDbInitialized() {
            return appliedMigrationNames != null || oldDbVersion != null;
        }

        public boolean isWfeConstantsDropped() {
            return appliedMigrationNames != null && appliedMigrationNames.contains(RemoveWfeConstants.class.getSimpleName());
        }

    }


    @Autowired
    private ManualTransactionManager txManager;


    /**
     * @return Null if DB_MIGRATION table doesn't exist.
     */
    private HashSet<String> queryAppliedMigrationNames() throws Exception {
        return txManager.callInTransaction(new ManualTransactionManager.TxCallable<HashSet<String>>() {
            @Override
            public HashSet<String> call(Connection conn) {
                HashSet<String> result = null;
                try (val stmt = conn.createStatement()) {
                    val rs = stmt.executeQuery("select name, when_finished from db_migration");
                    // Table exists:
                    result = new HashSet<>();
                    while (rs.next()) {
                        String name = rs.getString(1);
                        if (rs.getTimestamp(2) == null) {
                            throw new Exception("Migration \"" + name + "\" was unfinished, database is possibly inconsistent, aborting.");
                        }
                        result.add(name);
                    }
                    return result;
                } catch (Throwable e) {
                    if (result != null) {
                        throw new RuntimeException("Failed to fetch DB_MIGRATION table", e);
                    }
                    return null;
                }
            }
        });
    }


    /**
     * TODO Remove in WFE 5.
     *
     * @return Null if table WFE_CONSTANTS doesn't exist or doesn't contain DB version row.
     */
    private Integer queryOldDbVersion() throws Exception {
        return txManager.callInTransaction(new ManualTransactionManager.TxCallable<Integer>() {
            @Override
            public Integer call(Connection conn) {
                try (val stmt = conn.createStatement()) {
                    val rs = stmt.executeQuery("select value from wfe_constants where name='ru.runa.database_version'");
                    if (rs.next()) {
                        return Integer.parseInt(rs.getString(1));
                    } else {
                        return null;
                    }
                } catch (Throwable e) {
                    return null;
                }
            }
        });
    }


    public Context checkDbInitialized() throws Exception {
        val ctx = new Context();
        ctx.appliedMigrationNames = queryAppliedMigrationNames();
        if (!ctx.isWfeConstantsDropped()) {
            ctx.oldDbVersion = queryOldDbVersion();
        }
        return ctx;
    }


    /**
     * Runs special auto-generated DbMigration0 on empty database.
     */
    public void runDbMigration0() throws Exception {
        log.info("Applying migration DbMigration0 (initializing empty database)...");
        txManager.runInTransaction(new TxRunnable() {
            @Override
            public void run(Connection conn) throws Exception {
                val migration = ApplicationContextFactory.createAutowiredBean(DbMigration0.class);
                migration.execute();
            }
        });
    }


    /**
     * Pure JDBC.
     * Creates & uses DB_MIGRATION table.
     * On first run, uses database version stored in WFE_CONSTANTS.
     * After first run, database version stored in WFE_CONSTANTS is no longer relevant, must not be used, can be deleted.
     *
     * @return List of applied migrations, in the order.
     */
    public List<DbMigration> runAll(Context ctx, List<Class<? extends DbMigration>> migrations) throws Exception {
        if (!ctx.isDbInitialized()) {
            // Might have been initialized by DbMigration0.
            ctx.appliedMigrationNames = queryAppliedMigrationNames();
            if (!ctx.isWfeConstantsDropped()) {
                ctx.oldDbVersion = queryOldDbVersion();
            }
        }

        // If table DB_MIGRATION does not exist, create it and populate with migrations already applied,
        // using old DB "version" stored in WFE_CONSTANTS. From now on, this old "version" won't be neither used nor updated.
        if (ctx.appliedMigrationNames == null) {
            log.info("First run detected, creating and populating table DB_MIGRATION...");
            txManager.runInTransaction(new TxRunnable() {
                @Override
                public void run(Connection conn) throws Exception {

                    String sqlTimestampTypeName;
                    switch (ApplicationContextFactory.getDbType()) {
                        case MSSQL:
                        case MYSQL:
                            sqlTimestampTypeName = "datetime";
                            break;
                        default:
                            sqlTimestampTypeName = "timestamp";
                    }
                    executeUpdates(conn, "create table db_migration (" +
                            "name varchar(255) not null primary key, " +
                            "when_started " + sqlTimestampTypeName + " not null, " +
                            "when_finished " + sqlTimestampTypeName +
                            ")");

                    // TODO Remove in WFE 5. Migrations already applied must be inserted by DbMigration0.
                    if (ctx.oldDbVersion == null) {
                        throw new Exception("Could not retrieve legacy DB version from WFE_CONSTANTS");
                    }
                    if (ctx.oldDbVersion > migrations.size()) {
                        throw new Exception("dbVersion " + ctx.oldDbVersion + " > migrations.size() " + migrations.size());
                    }

                    ctx.appliedMigrationNames = new HashSet<>();
                    val now = new Timestamp(System.currentTimeMillis());
                    try (val stmt = conn.prepareStatement("insert into db_migration (name, when_started, when_finished) values (?, ?, ?)")) {
                        for (int i = 0; i < ctx.oldDbVersion; i++) {
                            val c = migrations.get(i);
                            if (UnsupportedPatch.class.isAssignableFrom(c) || EmptyPatch.class.isAssignableFrom(c)) {
                                continue;
                            }
                            val name = c.getSimpleName();
                            if (ctx.appliedMigrationNames.contains(name)) {
                                log.warn("Ignoring duplicate migration name in list: " + name);
                                continue;
                            }
                            stmt.setString(1, name);
                            stmt.setTimestamp(2, now);
                            stmt.setTimestamp(3, now);
                            stmt.executeUpdate();
                            ctx.appliedMigrationNames.add(name);
                        }
                    }
                }
            });
        }

        // Now run unapplied migrations, each in separate transaction.
        val appliedNow = new ArrayList<DbMigration>();
        long whenAllStarted = System.currentTimeMillis();
        for (Class<? extends DbMigration> c : migrations) {
            if (UnsupportedPatch.class.isAssignableFrom(c) || EmptyPatch.class.isAssignableFrom(c)) {
                continue;
            }
            val name = c.getSimpleName();
            if (ctx.appliedMigrationNames.contains(name)) {
                continue;
            }
            val migration = ApplicationContextFactory.createAutowiredBean(c);

            log.info("Applying migration " + name + "...");
            txManager.runInTransaction(new TxRunnable() {
                @Override
                public void run(Connection conn) throws Exception {
                    // No reason to split single insert into insert + update(when_finished) both performed in single transaction for PostgreSQL,
                    // but Oracle does not have transactional DDL. TODO Or maybe perform this first insert in separate transaction?
                    try (val stmt = conn.prepareStatement("insert into db_migration (name, when_started) values (?, ?)")) {
                        stmt.setString(1, name);
                        stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        stmt.executeUpdate();
                    }
                }
            });
            txManager.runInTransaction(new TxRunnable() {
                @Override
                public void run(Connection conn) throws Exception {
                    migration.execute();
                }
            });
            txManager.runInTransaction(new TxRunnable() {
                @Override
                public void run(Connection conn) throws Exception {
                    try (val stmt = conn.prepareStatement("update db_migration set when_finished = ? where name = ?")) {
                        stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                        stmt.setString(2, name);
                        stmt.executeUpdate();
                    }
                }
            });
            ctx.appliedMigrationNames.add(name);
            appliedNow.add(migration);
        }

        log.info("Applied " + appliedNow.size() + " migration(s) in " + ((System.currentTimeMillis() - whenAllStarted) / 1000) + " second(s).");
        return appliedNow;
    }
}
