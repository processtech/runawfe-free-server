package ru.runa.wfe.script.common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.batch.ReplicateBatchPresentationOperation;
import ru.runa.wfe.script.botstation.AddConfigurationsToBotOperation;
import ru.runa.wfe.script.botstation.CreateBotOperation;
import ru.runa.wfe.script.botstation.CreateBotStationOperation;
import ru.runa.wfe.script.botstation.DeleteBotOperation;
import ru.runa.wfe.script.botstation.DeleteBotStationOperation;
import ru.runa.wfe.script.botstation.RemoveAllConfigurationsFromBotOperation;
import ru.runa.wfe.script.botstation.RemoveConfigurationsFromBotOperation;
import ru.runa.wfe.script.botstation.UpdateBotOperation;
import ru.runa.wfe.script.botstation.UpdateBotStationOperation;
import ru.runa.wfe.script.executor.AddExecutorsToGroupOperation;
import ru.runa.wfe.script.executor.CreateActorOperation;
import ru.runa.wfe.script.executor.CreateGroupOperation;
import ru.runa.wfe.script.executor.DeleteExecutorOperation;
import ru.runa.wfe.script.executor.DeleteExecutorsOperation;
import ru.runa.wfe.script.executor.RemoveExecutorsFromGroupOperation;
import ru.runa.wfe.script.executor.SetActorInactiveOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnActorOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnBotStationOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnDefinitionOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnGroupOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnProcessesOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnRelationGroupOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnRelationOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnReportOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnReportsOperation;
import ru.runa.wfe.script.permission.AddPermissionsOnSystemOperation;
import ru.runa.wfe.script.permission.RemoveAllPermissionsFromExecutorOperation;
import ru.runa.wfe.script.permission.RemoveAllPermissionsFromProcessDefinitionOperation;
import ru.runa.wfe.script.permission.RemoveAllPermissionsFromProcessesOperation;
import ru.runa.wfe.script.permission.RemoveAllPermissionsFromSystemOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnActorOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnBotStationOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnDefinitionOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnGroupOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnProcessesOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnRelationGroupOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnRelationOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnReportOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnReportsOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOnSystemOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnActorOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnBotStationOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnDefinitionOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnGroupOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnProcessesOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnRelationGroupOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnRelationOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnReportOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnReportsOperation;
import ru.runa.wfe.script.permission.SetPermissionsOnSystemOperation;
import ru.runa.wfe.script.processes.CancelProcessesOperation;
import ru.runa.wfe.script.processes.DeployProcessDefinitionOperation;
import ru.runa.wfe.script.processes.RedeployProcessDefinitionOperation;
import ru.runa.wfe.script.processes.RemoveProcessesOperation;
import ru.runa.wfe.script.relation.CreateRelationOperation;
import ru.runa.wfe.script.report.DeployReportOperation;
import ru.runa.wfe.script.substitution.ChangeSubstitutionOperation;

import com.google.common.collect.Lists;

@XmlTransient
public abstract class OperationsListContainer {
    @XmlElements({
            @XmlElement(name = CreateActorOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CreateActorOperation.class),
            @XmlElement(name = CreateGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CreateGroupOperation.class),
            @XmlElement(name = DeleteExecutorOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeleteExecutorOperation.class),
            @XmlElement(name = DeleteExecutorsOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeleteExecutorsOperation.class),
            @XmlElement(name = AddExecutorsToGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddExecutorsToGroupOperation.class),
            @XmlElement(name = RemoveExecutorsFromGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveExecutorsFromGroupOperation.class),
            @XmlElement(name = SetActorInactiveOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetActorInactiveOperation.class),

            @XmlElement(name = AddPermissionsOnActorOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnActorOperation.class),
            @XmlElement(name = RemovePermissionsOnActorOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnActorOperation.class),
            @XmlElement(name = SetPermissionsOnActorOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnActorOperation.class),
            @XmlElement(name = AddPermissionsOnGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnGroupOperation.class),
            @XmlElement(name = RemovePermissionsOnGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnGroupOperation.class),
            @XmlElement(name = SetPermissionsOnGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnGroupOperation.class),
            @XmlElement(name = RemoveAllPermissionsFromExecutorOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveAllPermissionsFromExecutorOperation.class),

            @XmlElement(name = AddPermissionsOnSystemOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnSystemOperation.class),
            @XmlElement(name = RemovePermissionsOnSystemOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnSystemOperation.class),
            @XmlElement(name = SetPermissionsOnSystemOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnSystemOperation.class),
            @XmlElement(name = RemoveAllPermissionsFromSystemOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveAllPermissionsFromSystemOperation.class),

            @XmlElement(name = DeployProcessDefinitionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeployProcessDefinitionOperation.class),
            @XmlElement(name = RedeployProcessDefinitionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RedeployProcessDefinitionOperation.class),
            @XmlElement(name = AddPermissionsOnDefinitionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnDefinitionOperation.class),
            @XmlElement(name = RemovePermissionsOnDefinitionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnDefinitionOperation.class),
            @XmlElement(name = SetPermissionsOnDefinitionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnDefinitionOperation.class),
            @XmlElement(name = RemoveAllPermissionsFromProcessDefinitionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveAllPermissionsFromProcessDefinitionOperation.class),

            @XmlElement(name = CancelProcessesOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CancelProcessesOperation.class),
            @XmlElement(name = "stopProcess", namespace = AdminScriptConstants.NAMESPACE, type = CancelProcessesOperation.class),
            @XmlElement(name = RemoveProcessesOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveProcessesOperation.class),
            @XmlElement(name = AddPermissionsOnProcessesOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnProcessesOperation.class),
            @XmlElement(name = RemovePermissionsOnProcessesOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnProcessesOperation.class),
            @XmlElement(name = SetPermissionsOnProcessesOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnProcessesOperation.class),
            @XmlElement(name = RemoveAllPermissionsFromProcessesOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveAllPermissionsFromProcessesOperation.class),

            @XmlElement(name = ChangeSubstitutionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = ChangeSubstitutionOperation.class),

            @XmlElement(name = CreateRelationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CreateRelationOperation.class),
            @XmlElement(name = "relation", namespace = AdminScriptConstants.NAMESPACE, type = CreateRelationOperation.class),
            @XmlElement(name = AddPermissionsOnRelationGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnRelationGroupOperation.class),
            @XmlElement(name = RemovePermissionsOnRelationGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnRelationGroupOperation.class),
            @XmlElement(name = SetPermissionsOnRelationGroupOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnRelationGroupOperation.class),
            @XmlElement(name = AddPermissionsOnRelationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnRelationOperation.class),
            @XmlElement(name = RemovePermissionsOnRelationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnRelationOperation.class),
            @XmlElement(name = SetPermissionsOnRelationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnRelationOperation.class),

            @XmlElement(name = CreateBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CreateBotStationOperation.class),
            @XmlElement(name = UpdateBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = UpdateBotStationOperation.class),
            @XmlElement(name = DeleteBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeleteBotStationOperation.class),
            @XmlElement(name = CreateBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CreateBotOperation.class),
            @XmlElement(name = UpdateBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = UpdateBotOperation.class),
            @XmlElement(name = DeleteBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeleteBotOperation.class),
            @XmlElement(name = AddConfigurationsToBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddConfigurationsToBotOperation.class),
            @XmlElement(name = RemoveConfigurationsFromBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveConfigurationsFromBotOperation.class),
            @XmlElement(name = RemoveAllConfigurationsFromBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveAllConfigurationsFromBotOperation.class),
            @XmlElement(name = AddPermissionsOnBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnBotStationOperation.class),
            @XmlElement(name = RemovePermissionsOnBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnBotStationOperation.class),
            @XmlElement(name = SetPermissionsOnBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnBotStationOperation.class),

            @XmlElement(name = DeployReportOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeployReportOperation.class),
            @XmlElement(name = AddPermissionsOnReportOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnReportOperation.class),
            @XmlElement(name = RemovePermissionsOnReportOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnReportOperation.class),
            @XmlElement(name = SetPermissionsOnReportOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnReportOperation.class),
            @XmlElement(name = AddPermissionsOnReportsOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOnReportsOperation.class),
            @XmlElement(name = RemovePermissionsOnReportsOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOnReportsOperation.class),
            @XmlElement(name = SetPermissionsOnReportsOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOnReportsOperation.class),

            @XmlElement(name = ReplicateBatchPresentationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = ReplicateBatchPresentationOperation.class),
            @XmlElement(name = CustomOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CustomOperation.class)

    })
    public List<ScriptOperation> operations = Lists.newArrayList();

    /**
     * Get external resources, required to execute operation.
     * 
     * @return Return list of required external resources or empty list if no external resources is required.
     */
    public List<String> getExternalResourceNames() {
        ArrayList<String> result = Lists.newArrayList();
        for (ScriptOperation operation : operations) {
            result.addAll(operation.getExternalResources());
        }
        return result;
    }
}
