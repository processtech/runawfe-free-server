package ru.runa.wfe.commons.dbmigration;

import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class InitializerLogic implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ArrayList<Class<? extends DbMigration>> dbMigrations;
    @Autowired
    private DbTransactionalInitializer dbTransactionalInitializer;
    @Autowired
    private DbMigrationManager dbMigrationManager;
    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            if (initialized.getAndSet(true)) {
                return;
            }
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
        } catch (Exception e) {
            Throwables.propagate(e);
        }
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
