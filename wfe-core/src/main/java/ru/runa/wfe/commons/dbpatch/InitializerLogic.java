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

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
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
import ru.runa.wfe.commons.dbpatch.impl.AddProcessExternalData;
import ru.runa.wfe.commons.dbpatch.impl.AddSequentialFlagToBot;
import ru.runa.wfe.commons.dbpatch.impl.AddSettingsTable;
import ru.runa.wfe.commons.dbpatch.impl.AddSubProcessIndexColumn;
import ru.runa.wfe.commons.dbpatch.impl.AddSubprocessBindingDatePatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTitleAndDepartmentColumnsToActorPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTokenErrorDataPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTokenMessageSelectorPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTransactionalBotSupport;
import ru.runa.wfe.commons.dbpatch.impl.AddVariableUniqueKeyPatch;
import ru.runa.wfe.commons.dbpatch.impl.CreateAdminScriptTables;
import ru.runa.wfe.commons.dbpatch.impl.CreateAggregatedLogsTables;
import ru.runa.wfe.commons.dbpatch.impl.CreateChatDB;
import ru.runa.wfe.commons.dbpatch.impl.CreateReportsTables;
import ru.runa.wfe.commons.dbpatch.impl.ExpandDescriptionsPatch;
import ru.runa.wfe.commons.dbpatch.impl.ExpandVarcharPatch;
import ru.runa.wfe.commons.dbpatch.impl.JbpmRefactoringPatch;
import ru.runa.wfe.commons.dbpatch.impl.NodeTypeChangePatch;
import ru.runa.wfe.commons.dbpatch.impl.PerformancePatch401;
import ru.runa.wfe.commons.dbpatch.impl.PermissionMappingPatch403;
import ru.runa.wfe.commons.dbpatch.impl.RefactorPermissionsStep1;
import ru.runa.wfe.commons.dbpatch.impl.RefactorPermissionsStep3;
import ru.runa.wfe.commons.dbpatch.impl.TaskCreateLogSeverityChangedPatch;
import ru.runa.wfe.commons.dbpatch.impl.TaskEndDateRemovalPatch;
import ru.runa.wfe.commons.dbpatch.impl.TaskOpenedByExecutorsPatch;
import ru.runa.wfe.commons.dbpatch.impl.TransitionLogPatch;

/**
 * Initial DB population and update during version change.
 *
 * @author Dofs
 */
public class InitializerLogic implements ApplicationListener<ContextRefreshedEvent> {
    protected static final Log log = LogFactory.getLog(InitializerLogic.class);
    private static final List<Class<? extends DbPatch>> dbPatches;
    @Autowired
    private DbTransactionalInitializer dbTransactionalInitializer;

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
        patches.add(AddProcessExternalData.class);
        // chat
        patches.add(CreateChatDB.class);
        dbPatches = Collections.unmodifiableList(patches);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            Integer databaseVersion = dbTransactionalInitializer.getDatabaseVersion();
            if (databaseVersion != null) {
                applyPatches(databaseVersion);
            } else {
                log.info("initializing database");
                SchemaExport schemaExport = new SchemaExport(ApplicationContextFactory.getConfiguration());
                schemaExport.execute(true, true, false, true);
                dbTransactionalInitializer.initialize(dbPatches.size());
            }
            dbTransactionalInitializer.initPermissions();
            if (databaseVersion != null) {
                postProcessPatches(databaseVersion);
            }
            dbTransactionalInitializer.initLocalizations();
            if (DatabaseProperties.isDatabaseSettingsEnabled()) {
                PropertyResources.setDatabaseAvailable(true);
            }
            log.info("initialization completed");
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    /**
     * Apply patches to initialized database.
     */
    private void applyPatches(int databaseVersion) {

        // databaseVersion--;// для патча чата, временно!

        log.info("Database version: " + databaseVersion + ", code version: " + dbPatches.size());
        while (databaseVersion < dbPatches.size()) {
            DbPatch patch = null;
            try {
                patch = ApplicationContextFactory.createAutowiredBean(dbPatches.get(databaseVersion));
                databaseVersion++;
                log.info("Applying patch " + patch + " (" + databaseVersion + ")");
                dbTransactionalInitializer.execute(patch, databaseVersion);
                log.info("Patch " + patch + "(" + databaseVersion + ") is applied to database successfully.");
            } catch (Throwable th) {
                throw new InternalApplicationException("Can't apply patch " + patch + "(" + databaseVersion + ").", th);
            }
        }
    }

    private void postProcessPatches(Integer databaseVersion) {
        while (databaseVersion < dbPatches.size()) {
            DbPatch patch = ApplicationContextFactory.createAutowiredBean(dbPatches.get(databaseVersion));
            databaseVersion++;
            if (patch instanceof DbPatchPostProcessor) {
                log.info("Post-processing patch " + patch + " (" + databaseVersion + ")");
                try {
                    dbTransactionalInitializer.postExecute((DbPatchPostProcessor) patch);
                    log.info("Patch " + patch.getClass().getName() + "(" + databaseVersion + ") is post-processed successfully.");
                } catch (Throwable th) {
                    log.error("Can't post-process patch " + patch.getClass().getName() + "(" + databaseVersion + ").", th);
                    break;
                }
            }
        }
    }
}