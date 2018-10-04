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
import java.util.Arrays;
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
import ru.runa.wfe.commons.dbmigration.impl.AddAggregatedTaskIndexPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddAssignDateColumnPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddBatchPresentationIsSharedPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddColumnForEmbeddedBotTaskFileName;
import ru.runa.wfe.commons.dbmigration.impl.AddColumnsToSubstituteEscalatedTasksPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddCreateDateColumns;
import ru.runa.wfe.commons.dbmigration.impl.AddDeploymentAuditPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddDueDateExpressionToJobAndTask;
import ru.runa.wfe.commons.dbmigration.impl.AddEmbeddedFileForBotTask;
import ru.runa.wfe.commons.dbmigration.impl.AddHierarchyProcess;
import ru.runa.wfe.commons.dbmigration.impl.AddMultiTaskIndexToTaskPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddNodeIdToProcessLogPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddParentProcessIdPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddProcessAndTokenExecutionStatusPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddSequentialFlagToBot;
import ru.runa.wfe.commons.dbmigration.impl.AddSettingsTable;
import ru.runa.wfe.commons.dbmigration.impl.AddSubProcessIndexColumn;
import ru.runa.wfe.commons.dbmigration.impl.AddSubprocessBindingDatePatch;
import ru.runa.wfe.commons.dbmigration.impl.AddSubprocessRootIdColumn;
import ru.runa.wfe.commons.dbmigration.impl.AddTitleAndDepartmentColumnsToActorPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTokenErrorDataPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTokenMessageSelectorPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTransactionalBotSupport;
import ru.runa.wfe.commons.dbmigration.impl.AddVariableUniqueKeyPatch;
import ru.runa.wfe.commons.dbmigration.impl.CreateAdminScriptTables;
import ru.runa.wfe.commons.dbmigration.impl.CreateAggregatedLogsTables;
import ru.runa.wfe.commons.dbmigration.impl.CreateReportsTables;
import ru.runa.wfe.commons.dbmigration.impl.ExpandDescriptionsPatch;
import ru.runa.wfe.commons.dbmigration.impl.ExpandVarcharPatch;
import ru.runa.wfe.commons.dbmigration.impl.JbpmRefactoringPatch;
import ru.runa.wfe.commons.dbmigration.impl.NodeTypeChangePatch;
import ru.runa.wfe.commons.dbmigration.impl.PerformancePatch401;
import ru.runa.wfe.commons.dbmigration.impl.PermissionMappingPatch403;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsStep1;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsStep3;
import ru.runa.wfe.commons.dbmigration.impl.RenameProcessesBatchPresentationCategories;
import ru.runa.wfe.commons.dbmigration.impl.RenameProcessesBatchPresentationClassTypes;
import ru.runa.wfe.commons.dbmigration.impl.RenameSequences;
import ru.runa.wfe.commons.dbmigration.impl.SplitProcessDefinitionVersion;
import ru.runa.wfe.commons.dbmigration.impl.SplitProcessDefinitionVersionCheck;
import ru.runa.wfe.commons.dbmigration.impl.SupportProcessArchiving;
import ru.runa.wfe.commons.dbmigration.impl.TaskCreateLogSeverityChangedPatch;
import ru.runa.wfe.commons.dbmigration.impl.TaskEndDateRemovalPatch;
import ru.runa.wfe.commons.dbmigration.impl.TaskOpenedByExecutorsPatch;
import ru.runa.wfe.commons.dbmigration.impl.TransitionLogPatch;

/**
 * Initial DB population and update during version change.
 *
 * @author Dofs
 */
@Component
@CommonsLog
public class InitializerLogic implements ApplicationListener<ContextRefreshedEvent> {

    private static final List<Class<? extends DbMigration>> dbPatches = Arrays.asList(
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            UnsupportedPatch.class,
            // version 20
            // 4.0.0
            AddHierarchyProcess.class,
            JbpmRefactoringPatch.class,
            TransitionLogPatch.class,
            // 4.0.1
            PerformancePatch401.class,
            TaskEndDateRemovalPatch.class,
            // 4.0.1
            PermissionMappingPatch403.class,
            // 4.0.5
            NodeTypeChangePatch.class,
            ExpandDescriptionsPatch.class,
            // 4.0.6
            TaskOpenedByExecutorsPatch.class,
            // 4.1.0
            AddNodeIdToProcessLogPatch.class,
            AddSubProcessIndexColumn.class,
            // 4.1.1
            AddCreateDateColumns.class,
            // 4.2.0
            AddEmbeddedFileForBotTask.class,
            AddColumnForEmbeddedBotTaskFileName.class,
            AddSettingsTable.class,
            AddSequentialFlagToBot.class,
            CreateAggregatedLogsTables.class,
            TaskCreateLogSeverityChangedPatch.class,
            AddColumnsToSubstituteEscalatedTasksPatch.class,
            // 4.2.1
            AddMultiTaskIndexToTaskPatch.class,
            // 4.2.2
            AddDeploymentAuditPatch.class,
            // 4.3.0
            AddAggregatedTaskIndexPatch.class,
            AddParentProcessIdPatch.class,
            CreateReportsTables.class,
            AddDueDateExpressionToJobAndTask.class,
            AddBatchPresentationIsSharedPatch.class,
            ExpandVarcharPatch.class,
            AddProcessAndTokenExecutionStatusPatch.class,
            CreateAdminScriptTables.class,
            AddVariableUniqueKeyPatch.class,
            AddTokenErrorDataPatch.class,
            AddTitleAndDepartmentColumnsToActorPatch.class,
            AddAssignDateColumnPatch.class,
            EmptyPatch.class,
            AddTokenMessageSelectorPatch.class,
            AddSubprocessBindingDatePatch.class,
            AddTransactionalBotSupport.class,
            RefactorPermissionsStep1.class,
            RefactorPermissionsStep3.class,
            SplitProcessDefinitionVersionCheck.class,
            SplitProcessDefinitionVersion.class,
            AddSubprocessRootIdColumn.class,
            SupportProcessArchiving.class,
            RenameProcessesBatchPresentationCategories.class,
            RenameProcessesBatchPresentationClassTypes.class,
            RenameSequences.class
    );

    @Autowired
    private DbTransactionalInitializer dbTransactionalInitializer;
    @Autowired
    private DbMigrationManager dbMigrationManager;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            val mmContext = dbMigrationManager.checkDbInitialized();
            if (!mmContext.isDbInitialized()) {
                dbMigrationManager.runDbMigration0();
                dbTransactionalInitializer.insertInitialData();
            }
            val appliedMigrations = dbMigrationManager.runAll(mmContext, dbPatches);
            dbTransactionalInitializer.initPermissions();
            postProcessPatches(appliedMigrations);
            dbTransactionalInitializer.initLocalizations();
            if (DatabaseProperties.isDynamicSettingsEnabled()) {
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
