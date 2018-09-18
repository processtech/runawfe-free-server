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
package ru.runa.wfe.commons.dbpatch;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.DatabaseProperties;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.dbpatch.impl.AddAggregatedTaskIndexPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddAssignDateColumnPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddBatchPresentationIsSharedPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddColumnForEmbeddedBotTaskFileName;
import ru.runa.wfe.commons.dbpatch.impl.AddColumnsToSubstituteEscalatedTasksPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddCreateDateColumns;
import ru.runa.wfe.commons.dbpatch.impl.AddDeploymentAuditPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddDueDateExpressionToJobAndTask;
import ru.runa.wfe.commons.dbpatch.impl.AddEmbeddedFileForBotTask;
import ru.runa.wfe.commons.dbpatch.impl.AddHierarchyProcess;
import ru.runa.wfe.commons.dbpatch.impl.AddMultiTaskIndexToTaskPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddNodeIdToProcessLogPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddParentProcessIdPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddProcessAndTokenExecutionStatusPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddSequentialFlagToBot;
import ru.runa.wfe.commons.dbpatch.impl.AddSettingsTable;
import ru.runa.wfe.commons.dbpatch.impl.AddSubProcessIndexColumn;
import ru.runa.wfe.commons.dbpatch.impl.AddSubprocessBindingDatePatch;
import ru.runa.wfe.commons.dbpatch.impl.AddSubprocessRootIdColumn;
import ru.runa.wfe.commons.dbpatch.impl.AddTitleAndDepartmentColumnsToActorPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTokenErrorDataPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTokenMessageSelectorPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTransactionalBotSupport;
import ru.runa.wfe.commons.dbpatch.impl.AddVariableUniqueKeyPatch;
import ru.runa.wfe.commons.dbpatch.impl.CreateAdminScriptTables;
import ru.runa.wfe.commons.dbpatch.impl.CreateAggregatedLogsTables;
import ru.runa.wfe.commons.dbpatch.impl.CreateReportsTables;
import ru.runa.wfe.commons.dbpatch.impl.ExpandDescriptionsPatch;
import ru.runa.wfe.commons.dbpatch.impl.ExpandVarcharPatch;
import ru.runa.wfe.commons.dbpatch.impl.JbpmRefactoringPatch;
import ru.runa.wfe.commons.dbpatch.impl.NodeTypeChangePatch;
import ru.runa.wfe.commons.dbpatch.impl.PerformancePatch401;
import ru.runa.wfe.commons.dbpatch.impl.PermissionMappingPatch403;
import ru.runa.wfe.commons.dbpatch.impl.RefactorPermissionsStep1;
import ru.runa.wfe.commons.dbpatch.impl.RefactorPermissionsStep3;
import ru.runa.wfe.commons.dbpatch.impl.RenameProcessesBatchPresentationCategories;
import ru.runa.wfe.commons.dbpatch.impl.SplitProcessDefinitionVersion;
import ru.runa.wfe.commons.dbpatch.impl.SupportProcessArchiving;
import ru.runa.wfe.commons.dbpatch.impl.TaskCreateLogSeverityChangedPatch;
import ru.runa.wfe.commons.dbpatch.impl.TaskEndDateRemovalPatch;
import ru.runa.wfe.commons.dbpatch.impl.TaskOpenedByExecutorsPatch;
import ru.runa.wfe.commons.dbpatch.impl.TransitionLogPatch;

/**
 * Initial DB population and update during version change.
 *
 * @author Dofs
 */
@Component
@CommonsLog
public class InitializerLogic implements ApplicationListener<ContextRefreshedEvent> {
    private static final List<Class<? extends DbPatch>> dbPatches;

    @Autowired
    private DbTransactionalInitializer dbTransactionalInitializer;
    @Autowired
    private DbMigrationManager dbMigrationManager;

    static {
        List<Class<? extends DbPatch>> patches = Lists.newArrayList();
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        // version 20
        // 4.0.0
        patches.add(AddHierarchyProcess.class);
        patches.add(JbpmRefactoringPatch.class);
        patches.add(TransitionLogPatch.class);
        // 4.0.1
        patches.add(PerformancePatch401.class);
        patches.add(TaskEndDateRemovalPatch.class);
        // 4.0.1
        patches.add(PermissionMappingPatch403.class);
        // 4.0.5
        patches.add(NodeTypeChangePatch.class);
        patches.add(ExpandDescriptionsPatch.class);
        // 4.0.6
        patches.add(TaskOpenedByExecutorsPatch.class);
        // 4.1.0
        patches.add(AddNodeIdToProcessLogPatch.class);
        patches.add(AddSubProcessIndexColumn.class);
        // 4.1.1
        patches.add(AddCreateDateColumns.class);
        // 4.2.0
        patches.add(AddEmbeddedFileForBotTask.class);
        patches.add(AddColumnForEmbeddedBotTaskFileName.class);
        patches.add(AddSettingsTable.class);
        patches.add(AddSequentialFlagToBot.class);
        patches.add(CreateAggregatedLogsTables.class);
        patches.add(TaskCreateLogSeverityChangedPatch.class);
        patches.add(AddColumnsToSubstituteEscalatedTasksPatch.class);
        // 4.2.1
        patches.add(AddMultiTaskIndexToTaskPatch.class);
        // 4.2.2
        patches.add(AddDeploymentAuditPatch.class);
        // 4.3.0
        patches.add(AddAggregatedTaskIndexPatch.class);
        patches.add(AddParentProcessIdPatch.class);
        patches.add(CreateReportsTables.class);
        patches.add(AddDueDateExpressionToJobAndTask.class);
        patches.add(AddBatchPresentationIsSharedPatch.class);
        patches.add(ExpandVarcharPatch.class);
        patches.add(AddProcessAndTokenExecutionStatusPatch.class);
        patches.add(CreateAdminScriptTables.class);
        patches.add(AddVariableUniqueKeyPatch.class);
        patches.add(AddTokenErrorDataPatch.class);
        patches.add(AddTitleAndDepartmentColumnsToActorPatch.class);
        patches.add(AddAssignDateColumnPatch.class);
        patches.add(EmptyPatch.class);
        patches.add(AddTokenMessageSelectorPatch.class);
        patches.add(AddSubprocessBindingDatePatch.class);
        patches.add(AddTransactionalBotSupport.class);
        patches.add(RefactorPermissionsStep1.class);
        patches.add(RefactorPermissionsStep3.class);
        patches.add(SplitProcessDefinitionVersion.class);
        patches.add(AddSubprocessRootIdColumn.class);
        patches.add(SupportProcessArchiving.class);
        patches.add(RenameProcessesBatchPresentationCategories.class);
        dbPatches = Collections.unmodifiableList(patches);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            val mmContext = dbMigrationManager.checkDbInitialized();
            if (!mmContext.isDbInitialized()) {
                dbMigrationManager.runDbPatch0();
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
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private void postProcessPatches(List<DbPatch> appliedMigrations) {
        int done = 0;
        long whenStarted = System.currentTimeMillis();
        for (val m : appliedMigrations) {
            if (m instanceof DbPatchPostProcessor) {
                log.info("Post-processing migration " + m + "...");
                try {
                    dbTransactionalInitializer.postExecute((DbPatchPostProcessor) m);
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
