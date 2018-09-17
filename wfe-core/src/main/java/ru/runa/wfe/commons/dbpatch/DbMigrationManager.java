package ru.runa.wfe.commons.dbpatch;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ManualTransactionManager;
import ru.runa.wfe.commons.ManualTransactionManager.TxRunnable;

@Component
@CommonsLog
public class DbMigrationManager {

    @Autowired
    private ManualTransactionManager txManager;


    /**
     * Pure JDBC.
     * Creates & uses DB_MIGRATION table.
     * On first run, uses database version stored in WFE_CONSTANTS.
     * After first run, database version stored in WFE_CONSTANTS is no longer relevant, must not be used, can be deleted.
     */
    public void runAll(List<Class<? extends DbPatch>> migrations) throws Exception {

        // Tried to do this in @PostConstruct but got NullPointerException, looks like context was not completely initialized.
        String sqlTimestampTypeName;
        switch (ApplicationContextFactory.getDbType()) {
            case MSSQL:
                sqlTimestampTypeName = "datetime";
                break;
            default:
                sqlTimestampTypeName = "timestamp";
        }

        // Check if table DB_MIGRATION exists, and load migration names which are already applied.
        boolean isFirstRun = false;
        val appliedNames = new HashSet<String>();
        try {
            txManager.runInTransaction(new TxRunnable() {
                @Override
                public void run(Connection conn) throws Exception {
                    try (val stmt = conn.createStatement()) {
                        val rs = stmt.executeQuery("select name from db_migration");
                        while (rs.next()) {
                            appliedNames.add(rs.getString(1));
                        }
                    }
                }
            });
        } catch (Throwable e) {
            isFirstRun = true;
            if (!appliedNames.isEmpty()) {
                throw new IllegalStateException("isFirstRun && !appliedNames.isEmpty", e);
            }
        }

        // If table DB_MIGRATION did not exist, create it and populate with migrations already applied,
        // using DB version stored in WFE_CONSTANTS. From now on, this version stored in WFE_CONSTANTS won't be used nor updated.
        if (isFirstRun) {
            log.info("First run detected, creating and populating table DB_MIGRATION...");
            txManager.runInTransaction(new TxRunnable() {
                @Override
                public void run(Connection conn) throws Exception {
                    executeUpdates(conn, "create table db_migration (" +
                            "name varchar(255) not null primary key, " +
                            "when_started " + sqlTimestampTypeName + " not null, " +
                            "when_finished " + sqlTimestampTypeName +
                            ")");

                    int dbVersion;
                    try (val stmt = conn.createStatement()) {
                        val rs = stmt.executeQuery("select value from wfe_constants where name='ru.runa.database_version'");
                        if (!rs.next()) {
                            throw new RuntimeException("No rows");
                        }
                        dbVersion = Integer.parseInt(rs.getString(1));

                    } catch (Throwable e) {
                        throw new RuntimeException("Could not get database version from WFE_CONSTANTS", e);
                    }
                    if (dbVersion > migrations.size()) {
                        throw new RuntimeException("dbVersion " + dbVersion + " > migrations.size() " + migrations.size());
                    }

                    try (val stmt = conn.prepareStatement("insert into db_migration (name, when_started, when_finished) values (?, ?, ?)")) {
                        val now = new Timestamp(System.currentTimeMillis());
                        for (int i = 0;  i < dbVersion;  i++) {
                            val c = migrations.get(i);
                            if (UnsupportedPatch.class.isAssignableFrom(c) || EmptyPatch.class.isAssignableFrom(c)) {
                                continue;
                            }
                            val name = c.getSimpleName();
                            if (appliedNames.contains(name)) {
                                log.warn("Ignoring duplicate migration name in list: " + name);
                                continue;
                            }
                            stmt.setString(1, name);
                            stmt.setTimestamp(2, now);
                            stmt.setTimestamp(3, now);
                            stmt.executeUpdate();
                            appliedNames.add(name);
                        }
                    }
                }
            });
        }

        // Now run unapplied migrations, each in separate transaction.
        int appliedCount = 0;
        long whenAllStarted = System.currentTimeMillis();
        for (int i = 0;  i < migrations.size();  i++) {
            val c = migrations.get(i);
            if (UnsupportedPatch.class.isAssignableFrom(c) || EmptyPatch.class.isAssignableFrom(c)) {
                continue;
            }
            val name = c.getSimpleName();
            if (appliedNames.contains(name)) {
                continue;
            }

            log.info("Applying migration " + name + "...");
            txManager.runInTransaction(new TxRunnable() {
                @Override
                public void run(Connection conn) throws Exception {
                    // No reason to split single insert into insert + update(when_finished) both performed in single transaction for PostgreSQL,
                    // but Oracle does not have transactional DDL.
                    try (val stmt = conn.prepareStatement("insert into db_migration (name, when_started) values (?, ?)")) {
                        stmt.setString(1, name);
                        stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        stmt.executeUpdate();
                    }

                    val migration = ApplicationContextFactory.createAutowiredBean(c);
                    migration.execute();

                    try (val stmt = conn.prepareStatement("update db_migration set when_finished = ? where name = ?")) {
                        stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                        stmt.setString(2, name);
                        stmt.executeUpdate();
                    }
                }
            });
            appliedCount++;
            appliedNames.add(name);
        }

        log.info("Applied " + appliedCount + " migration(s) in " + ((System.currentTimeMillis() - whenAllStarted) / 1000) + " second(s).");
    }
}
