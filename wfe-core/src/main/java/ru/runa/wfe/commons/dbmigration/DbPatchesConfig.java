package ru.runa.wfe.commons.dbmigration;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import ru.runa.wfe.commons.dbmigration.impl.AddProcessExternalData;
import ru.runa.wfe.commons.dbmigration.impl.AddSequentialFlagToBot;
import ru.runa.wfe.commons.dbmigration.impl.AddSettingsTable;
import ru.runa.wfe.commons.dbmigration.impl.AddSubProcessIndexColumn;
import ru.runa.wfe.commons.dbmigration.impl.AddSubprocessBindingDatePatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTitleAndDepartmentColumnsToActorPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTokenErrorDataPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTokenMessageSelectorPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTransactionalBotSupport;
import ru.runa.wfe.commons.dbmigration.impl.AddVariableUniqueKeyPatch;
import ru.runa.wfe.commons.dbmigration.impl.CreateAdminScriptTables;
import ru.runa.wfe.commons.dbmigration.impl.CreateAggregatedLogsTables;
import ru.runa.wfe.commons.dbmigration.impl.CreateChatDbPatch;
import ru.runa.wfe.commons.dbmigration.impl.CreateReportsTables;
import ru.runa.wfe.commons.dbmigration.impl.ExpandDescriptionsPatch;
import ru.runa.wfe.commons.dbmigration.impl.ExpandVarcharPatch;
import ru.runa.wfe.commons.dbmigration.impl.JbpmRefactoringPatch;
import ru.runa.wfe.commons.dbmigration.impl.NodeTypeChangePatch;
import ru.runa.wfe.commons.dbmigration.impl.PerformancePatch401;
import ru.runa.wfe.commons.dbmigration.impl.PermissionMappingPatch403;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsBack;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsStep1;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsStep3;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsStep4;
import ru.runa.wfe.commons.dbmigration.impl.TaskCreateLogSeverityChangedPatch;
import ru.runa.wfe.commons.dbmigration.impl.TaskEndDateRemovalPatch;
import ru.runa.wfe.commons.dbmigration.impl.TaskOpenedByExecutorsPatch;
import ru.runa.wfe.commons.dbmigration.impl.TransitionLogPatch;

/**
 * @author Alekseev Mikhail
 * @since #1912
 */
@Configuration
public class DbPatchesConfig {
    @Bean
    public List<Class<? extends DbPatch>> dbPatches() {
        final List<Class<? extends DbPatch>> patches = new ArrayList<>(75); // please, update initial capacity on add/remove patch
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
        patches.add(RefactorPermissionsStep4.class);
        patches.add(EmptyPatch.class); // instead signals...
        patches.add(CreateChatDbPatch.class);
        patches.add(RefactorPermissionsBack.class);
        return patches;
    }
}
