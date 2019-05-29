package ru.runa.wfe.script.common;

import com.google.common.collect.Lists;
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
import ru.runa.wfe.script.datasource.DeployDataSourceOperation;
import ru.runa.wfe.script.executor.AddExecutorsToGroupOperation;
import ru.runa.wfe.script.executor.CreateActorOperation;
import ru.runa.wfe.script.executor.CreateGroupOperation;
import ru.runa.wfe.script.executor.DeleteExecutorOperation;
import ru.runa.wfe.script.executor.DeleteExecutorsOperation;
import ru.runa.wfe.script.executor.RemoveExecutorsFromGroupOperation;
import ru.runa.wfe.script.executor.SetActorInactiveOperation;
import ru.runa.wfe.script.permission.AddPermissionsOperation;
import ru.runa.wfe.script.permission.RemoveAllPermissionsOperation;
import ru.runa.wfe.script.permission.RemovePermissionsOperation;
import ru.runa.wfe.script.permission.SetPermissionsOperation;
import ru.runa.wfe.script.processes.CancelProcessesOperation;
import ru.runa.wfe.script.processes.DeployProcessDefinitionOperation;
import ru.runa.wfe.script.processes.RedeployProcessDefinitionOperation;
import ru.runa.wfe.script.processes.RemoveProcessesOperation;
import ru.runa.wfe.script.relation.CreateRelationOperation;
import ru.runa.wfe.script.report.DeployReportOperation;
import ru.runa.wfe.script.substitution.ChangeSubstitutionOperation;

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

            @XmlElement(name = DeployProcessDefinitionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeployProcessDefinitionOperation.class),
            @XmlElement(name = RedeployProcessDefinitionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RedeployProcessDefinitionOperation.class),

            @XmlElement(name = CancelProcessesOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CancelProcessesOperation.class),
            @XmlElement(name = "stopProcess", namespace = AdminScriptConstants.NAMESPACE, type = CancelProcessesOperation.class),
            @XmlElement(name = RemoveProcessesOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveProcessesOperation.class),

            @XmlElement(name = ChangeSubstitutionOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = ChangeSubstitutionOperation.class),

            @XmlElement(name = CreateRelationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CreateRelationOperation.class),
            @XmlElement(name = "relation", namespace = AdminScriptConstants.NAMESPACE, type = CreateRelationOperation.class),

            @XmlElement(name = CreateBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CreateBotStationOperation.class),
            @XmlElement(name = UpdateBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = UpdateBotStationOperation.class),
            @XmlElement(name = DeleteBotStationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeleteBotStationOperation.class),
            @XmlElement(name = CreateBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CreateBotOperation.class),
            @XmlElement(name = UpdateBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = UpdateBotOperation.class),
            @XmlElement(name = DeleteBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeleteBotOperation.class),
            @XmlElement(name = AddConfigurationsToBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddConfigurationsToBotOperation.class),
            @XmlElement(name = RemoveConfigurationsFromBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveConfigurationsFromBotOperation.class),
            @XmlElement(name = RemoveAllConfigurationsFromBotOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveAllConfigurationsFromBotOperation.class),

            @XmlElement(name = DeployReportOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeployReportOperation.class),

            @XmlElement(name = ReplicateBatchPresentationOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = ReplicateBatchPresentationOperation.class),
            @XmlElement(name = CustomOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = CustomOperation.class),

            @XmlElement(name = DeployDataSourceOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = DeployDataSourceOperation.class),

            // Permissions manipulation:
            @XmlElement(name = AddPermissionsOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = AddPermissionsOperation.class),
            @XmlElement(name = RemovePermissionsOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemovePermissionsOperation.class),
            @XmlElement(name = RemoveAllPermissionsOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = RemoveAllPermissionsOperation.class),
            @XmlElement(name = SetPermissionsOperation.SCRIPT_NAME, namespace = AdminScriptConstants.NAMESPACE, type = SetPermissionsOperation.class),
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
