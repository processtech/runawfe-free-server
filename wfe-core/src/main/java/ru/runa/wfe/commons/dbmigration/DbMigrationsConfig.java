package ru.runa.wfe.commons.dbmigration;

import java.util.ArrayList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.runa.wfe.commons.dbmigration.impl.AddAggregatedTaskIndexPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddArchivedProcessExternalData;
import ru.runa.wfe.commons.dbmigration.impl.AddArchivedTokenNodeNameAndNodeEnterDateColumnsPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddAssignDateColumnPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddAsyncForArchivedTaskAndSubprocess;
import ru.runa.wfe.commons.dbmigration.impl.AddAsyncForTaskAndSubprocess;
import ru.runa.wfe.commons.dbmigration.impl.AddBatchPresentationIsSharedPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddChatRoomViewPatch;
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
import ru.runa.wfe.commons.dbmigration.impl.AddProcessLogCleanBeforeDateColumnPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddSequentialFlagToBot;
import ru.runa.wfe.commons.dbmigration.impl.AddSettingsTable;
import ru.runa.wfe.commons.dbmigration.impl.AddStartProcessTimerJob;
import ru.runa.wfe.commons.dbmigration.impl.AddStartProcessTimerJobRefactorRm2681;
import ru.runa.wfe.commons.dbmigration.impl.AddSubProcessIndexColumn;
import ru.runa.wfe.commons.dbmigration.impl.AddSubprocessBindingDatePatch;
import ru.runa.wfe.commons.dbmigration.impl.AddSubprocessRootIdColumn;
import ru.runa.wfe.commons.dbmigration.impl.AddTitleAndDepartmentColumnsToActorPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTokenErrorDataPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTokenMessageSelectorPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTokenNodeNameAndNodeEnterDateColumnsPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddTransactionalBotSupport;
import ru.runa.wfe.commons.dbmigration.impl.AddTransitionNameForTaskPatch;
import ru.runa.wfe.commons.dbmigration.impl.AddUuidAndDropBytesChatMessageFilePatch;
import ru.runa.wfe.commons.dbmigration.impl.AddVariableUniqueKeyPatch;
import ru.runa.wfe.commons.dbmigration.impl.CorrectChatRoomViewRenameColumn;
import ru.runa.wfe.commons.dbmigration.impl.CreateAdminScriptTables;
import ru.runa.wfe.commons.dbmigration.impl.CreateAggregatedLogsTables;
import ru.runa.wfe.commons.dbmigration.impl.CreateChatArchivePatch;
import ru.runa.wfe.commons.dbmigration.impl.CreateChatDbPatch;
import ru.runa.wfe.commons.dbmigration.impl.CreateDigitalSignatureTable;
import ru.runa.wfe.commons.dbmigration.impl.CreateEventSubprocessTriggerTable;
import ru.runa.wfe.commons.dbmigration.impl.CreateReportsTables;
import ru.runa.wfe.commons.dbmigration.impl.CreateSignalListenerAggregatedLogTable;
import ru.runa.wfe.commons.dbmigration.impl.CreateSignalTable;
import ru.runa.wfe.commons.dbmigration.impl.CreateStatisticReportTable;
import ru.runa.wfe.commons.dbmigration.impl.CreateTimerAggregatedLogTable;
import ru.runa.wfe.commons.dbmigration.impl.DeleteBatchPresentationsRm3017;
import ru.runa.wfe.commons.dbmigration.impl.DeleteBatchPresentationsRm3056;
import ru.runa.wfe.commons.dbmigration.impl.DropMessageNotNullConstraintPatch;
import ru.runa.wfe.commons.dbmigration.impl.DropQuotedMessageIdsPatch;
import ru.runa.wfe.commons.dbmigration.impl.EnlargeMessageMaxSizePatch;
import ru.runa.wfe.commons.dbmigration.impl.ExpandChatColumnsPatch;
import ru.runa.wfe.commons.dbmigration.impl.ExpandDescriptionsPatch;
import ru.runa.wfe.commons.dbmigration.impl.ExpandVarcharPatch;
import ru.runa.wfe.commons.dbmigration.impl.JbpmRefactoringPatch;
import ru.runa.wfe.commons.dbmigration.impl.NodeTypeChangePatch;
import ru.runa.wfe.commons.dbmigration.impl.PerformancePatch401;
import ru.runa.wfe.commons.dbmigration.impl.PermissionMappingPatch403;
import ru.runa.wfe.commons.dbmigration.impl.RecreateChatRoomView;
import ru.runa.wfe.commons.dbmigration.impl.RecreateChatRoomView2;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsBack;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsStep1;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsStep3;
import ru.runa.wfe.commons.dbmigration.impl.RefactorPermissionsStep4;
import ru.runa.wfe.commons.dbmigration.impl.RefactorProcessDefinitionsRm2681;
import ru.runa.wfe.commons.dbmigration.impl.RemoveWfeConstants;
import ru.runa.wfe.commons.dbmigration.impl.RenameColumnInChatMessageRecipientPatch;
import ru.runa.wfe.commons.dbmigration.impl.RenameProcessesBatchPresentationCategories;
import ru.runa.wfe.commons.dbmigration.impl.RenameProcessesBatchPresentationClassTypes;
import ru.runa.wfe.commons.dbmigration.impl.RenameSequences;
import ru.runa.wfe.commons.dbmigration.impl.SplitProcessDefinitionVersion;
import ru.runa.wfe.commons.dbmigration.impl.SupportProcessArchiving;
import ru.runa.wfe.commons.dbmigration.impl.SupportProcessArchivingBefore;
import ru.runa.wfe.commons.dbmigration.impl.TaskCreateLogSeverityChangedPatch;
import ru.runa.wfe.commons.dbmigration.impl.TaskEndDateRemovalPatch;
import ru.runa.wfe.commons.dbmigration.impl.TaskOpenedByExecutorsPatch;
import ru.runa.wfe.commons.dbmigration.impl.TransitionLogPatch;

/**
 * @author Alekseev Mikhail
 * @since #1912
 */
@Configuration
public class DbMigrationsConfig {
    @Bean
    public ArrayList<Class<? extends DbMigration>> dbMigrations() {
        final ArrayList<Class<? extends DbMigration>> dbMigrations = new ArrayList<>(100); // update initial capacity on add/remove patch
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        dbMigrations.add(UnsupportedPatch.class);
        // version 20
        // 4.0.0
        dbMigrations.add(AddHierarchyProcess.class);
        dbMigrations.add(JbpmRefactoringPatch.class);
        dbMigrations.add(TransitionLogPatch.class);
        // 4.0.1
        dbMigrations.add(PerformancePatch401.class);
        dbMigrations.add(TaskEndDateRemovalPatch.class);
        // 4.0.1
        dbMigrations.add(PermissionMappingPatch403.class);
        // 4.0.5
        dbMigrations.add(NodeTypeChangePatch.class);
        dbMigrations.add(ExpandDescriptionsPatch.class);
        // 4.0.6
        dbMigrations.add(TaskOpenedByExecutorsPatch.class);
        // 4.1.0
        dbMigrations.add(AddNodeIdToProcessLogPatch.class);
        dbMigrations.add(AddSubProcessIndexColumn.class);
        // 4.1.1
        dbMigrations.add(AddCreateDateColumns.class);
        // 4.2.0
        dbMigrations.add(AddEmbeddedFileForBotTask.class);
        dbMigrations.add(AddColumnForEmbeddedBotTaskFileName.class);
        dbMigrations.add(AddSettingsTable.class);
        dbMigrations.add(AddSequentialFlagToBot.class);
        dbMigrations.add(CreateAggregatedLogsTables.class);
        dbMigrations.add(TaskCreateLogSeverityChangedPatch.class);
        dbMigrations.add(AddColumnsToSubstituteEscalatedTasksPatch.class);
        // 4.2.1
        dbMigrations.add(AddMultiTaskIndexToTaskPatch.class);
        // 4.2.2
        dbMigrations.add(AddDeploymentAuditPatch.class);
        // 4.3.0
        dbMigrations.add(AddAggregatedTaskIndexPatch.class);
        dbMigrations.add(AddParentProcessIdPatch.class);
        dbMigrations.add(CreateReportsTables.class);
        dbMigrations.add(AddDueDateExpressionToJobAndTask.class);
        dbMigrations.add(AddBatchPresentationIsSharedPatch.class);
        dbMigrations.add(ExpandVarcharPatch.class);
        dbMigrations.add(AddProcessAndTokenExecutionStatusPatch.class);
        dbMigrations.add(CreateAdminScriptTables.class);
        dbMigrations.add(AddVariableUniqueKeyPatch.class);
        dbMigrations.add(AddTokenErrorDataPatch.class);
        dbMigrations.add(AddTitleAndDepartmentColumnsToActorPatch.class);
        dbMigrations.add(AddAssignDateColumnPatch.class);
        dbMigrations.add(EmptyPatch.class);
        dbMigrations.add(AddTokenMessageSelectorPatch.class);
        dbMigrations.add(AddSubprocessBindingDatePatch.class);
        dbMigrations.add(AddTransactionalBotSupport.class);
        dbMigrations.add(RefactorPermissionsStep1.class);
        dbMigrations.add(RefactorPermissionsStep3.class);
        dbMigrations.add(AddProcessExternalData.class);
        dbMigrations.add(RefactorPermissionsStep4.class);
        dbMigrations.add(EmptyPatch.class);
        dbMigrations.add(CreateChatDbPatch.class);
        dbMigrations.add(RefactorPermissionsBack.class);
        dbMigrations.add(RemoveWfeConstants.class);
        dbMigrations.add(CreateStatisticReportTable.class);
        // 4.4.3
        dbMigrations.add(AddProcessLogCleanBeforeDateColumnPatch.class);
        dbMigrations.add(AddTokenNodeNameAndNodeEnterDateColumnsPatch.class);
        dbMigrations.add(CreateTimerAggregatedLogTable.class);
        dbMigrations.add(CreateSignalListenerAggregatedLogTable.class);
        dbMigrations.add(AddUuidAndDropBytesChatMessageFilePatch.class);
        dbMigrations.add(DropQuotedMessageIdsPatch.class);
        dbMigrations.add(ExpandChatColumnsPatch.class);
        dbMigrations.add(AddChatRoomViewPatch.class);
        dbMigrations.add(EnlargeMessageMaxSizePatch.class);
        dbMigrations.add(DropMessageNotNullConstraintPatch.class);
        dbMigrations.add(RenameColumnInChatMessageRecipientPatch.class);
        dbMigrations.add(DeleteBatchPresentationsRm3017.class);
        dbMigrations.add(DeleteBatchPresentationsRm3056.class);
        dbMigrations.add(CreateSignalTable.class);
        // start develop patches
        dbMigrations.add(SplitProcessDefinitionVersion.class);
        dbMigrations.add(AddSubprocessRootIdColumn.class);
        dbMigrations.add(SupportProcessArchivingBefore.class);
        dbMigrations.add(SupportProcessArchiving.class);
        dbMigrations.add(RenameProcessesBatchPresentationCategories.class);
        dbMigrations.add(RenameProcessesBatchPresentationClassTypes.class);
        dbMigrations.add(RenameSequences.class);
        dbMigrations.add(AddArchivedProcessExternalData.class);
        dbMigrations.add(AddArchivedTokenNodeNameAndNodeEnterDateColumnsPatch.class);
        dbMigrations.add(CreateChatArchivePatch.class);
        dbMigrations.add(CorrectChatRoomViewRenameColumn.class);
        dbMigrations.add(AddAsyncForTaskAndSubprocess.class);
        dbMigrations.add(AddAsyncForArchivedTaskAndSubprocess.class);
        dbMigrations.add(RecreateChatRoomView.class);
        dbMigrations.add(CreateDigitalSignatureTable.class);
        // end develop patches
        dbMigrations.add(AddTransitionNameForTaskPatch.class); // depends on SupportProcessArchivingBefore
        dbMigrations.add(RefactorProcessDefinitionsRm2681.class);
        dbMigrations.add(RecreateChatRoomView2.class);
        dbMigrations.add(AddStartProcessTimerJob.class);
        dbMigrations.add(AddStartProcessTimerJobRefactorRm2681.class);
        dbMigrations.add(CreateEventSubprocessTriggerTable.class);
        return dbMigrations;
    }
}
