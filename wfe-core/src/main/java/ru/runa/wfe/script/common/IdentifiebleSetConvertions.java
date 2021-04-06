package ru.runa.wfe.script.common;

import com.google.common.collect.Sets;
import java.util.Set;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.SecuredObject;

public final class IdentifiebleSetConvertions {

    public static Set<SecuredObject> getExecutors(ScriptExecutionContext context, Set<String> executorNames) {
        Set<SecuredObject> executors = Sets.newHashSet();
        for (String executorName : executorNames) {
            executors.add(context.getExecutorLogic().getExecutor(context.getUser(), executorName));
        }
        return executors;
    }

    public static Set<SecuredObject> getActors(ScriptExecutionContext context, Set<String> actorNames) {
        Set<SecuredObject> actors = Sets.newHashSet();
        for (String actorName : actorNames) {
            actors.add(context.getExecutorLogic().getActor(context.getUser(), actorName));
        }
        return actors;
    }

    public static Set<SecuredObject> getGroups(ScriptExecutionContext context, Set<String> groupNames) {
        Set<SecuredObject> groups = Sets.newHashSet();
        for (String groupName : groupNames) {
            groups.add(context.getExecutorLogic().getGroup(context.getUser(), groupName));
        }
        return groups;
    }

    public static Set<SecuredObject> getProcessDefinitions(ScriptExecutionContext context, Set<String> processDefinitionNames) {
        Set<SecuredObject> processDefinitions = Sets.newHashSet();
        for (String definitionName : processDefinitionNames) {
            processDefinitions.add(context.getProcessDefinitionLogic().getLatestProcessDefinition(context.getUser(), definitionName));
        }
        return processDefinitions;
    }

    public static Set<SecuredObject> getProcesses(ScriptExecutionContext context, Set<String> processDefinitionNames) {
        Set<SecuredObject> processInstances = Sets.newHashSet();
        for (String definitionName : processDefinitionNames) {
            BatchPresentation batchPresentation = BatchPresentationFactory.CURRENT_PROCESSES.createNonPaged();
            int definitionNameIndex = batchPresentation.getType().getFieldIndex(CurrentProcessClassPresentation.DEFINITION_NAME);
            batchPresentation.getFilteredFields().put(definitionNameIndex, new StringFilterCriteria(definitionName));
            processInstances.addAll(context.getExecutionLogic().getProcesses(context.getUser(), batchPresentation));
        }
        return processInstances;
    }

    public static Set<SecuredObject> getReports(ScriptExecutionContext context, Set<String> reportNames) {
        Set<SecuredObject> reports = Sets.newHashSet();
        for (String reportName : reportNames) {
            reports.add(context.getReportLogic().getReportDefinition(context.getUser(), reportName));
        }
        return reports;
    }
}
