/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
            boolean isDbInitialized = mmContext.isDbInitialized();
            if (!isDbInitialized) {
                dbMigrationManager.runDbMigration0();
            }
            val appliedMigrations = dbMigrationManager.runAll(mmContext, dbMigrations);
            if (!isDbInitialized) {
                dbTransactionalInitializer.insertInitialData();
            }
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