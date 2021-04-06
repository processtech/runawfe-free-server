package ru.runa.wfe.commons.dbmigration;

import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.val;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.DatabaseProperties;
import ru.runa.wfe.commons.PropertyResources;

/**
 * Initial DB population and update during version change.
 *
 * @author Dofs
 */
@Component
@CommonsLog
public class InitializerLogic implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ArrayList<Class<? extends DbMigration>> dbMigrations;
    @Autowired
    private DbTransactionalInitializer dbTransactionalInitializer;
    @Autowired
    private DbMigrationManager dbMigrationManager;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            val mmContext = dbMigrationManager.checkDbInitialized();
            if (!mmContext.isDbInitialized()) {
                dbMigrationManager.runDbMigration0();
                dbTransactionalInitializer.insertInitialData();
            }
            val appliedMigrations = dbMigrationManager.runAll(mmContext, dbMigrations);
            dbTransactionalInitializer.initPermissions();
            postProcessPatches(appliedMigrations);
            dbTransactionalInitializer.initLocalizations();
            if (DatabaseProperties.isDatabaseSettingsEnabled()) {
                PropertyResources.setDatabaseAvailable(true);
            }
            log.info("Initialization completed.");
            initialized.set(true);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    private void postProcessPatches(List<DbMigration> appliedMigrations) {
        int done = 0;
        long whenStarted = System.currentTimeMillis();
        for (val m : appliedMigrations) {
            if (m instanceof DbMigrationPostProcessor) {
                log.info("Post-processing migration " + m + "...");
                try {
                    dbTransactionalInitializer.postExecute((DbMigrationPostProcessor) m);
                    done++;
                } catch (Throwable e) {
                    log.error("Post-processing migration " + m.getClass().getName() + " failed", e);
                    break;
                }
            }
        }
        log.info("Post-processed " + done + " migration(s) in " + ((System.currentTimeMillis() - whenStarted) / 1000) + " second(s).");
    }
}
