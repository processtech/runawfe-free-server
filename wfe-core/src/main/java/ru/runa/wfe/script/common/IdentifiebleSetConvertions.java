package ru.runa.wfe.script.common;

import java.util.Set;

import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.Identifiable;

import com.google.common.collect.Sets;

public final class IdentifiebleSetConvertions {

    public static Set<Identifiable> getExecutors(ScriptExecutionContext context, Set<String> executorNames) {
        Set<Identifiable> executors = Sets.newHashSet();
        for (String executorName : executorNames) {
            executors.add(context.getExecutorLogic().getExecutor(context.getUser(), executorName));
        }
        return executors;
    }

    public static Set<Identifiable> getActors(ScriptExecutionContext context, Set<String> actorNames) {
        Set<Identifiable> actors = Sets.newHashSet();
        for (String actorName : actorNames) {
            actors.add(context.getExecutorLogic().getActor(context.getUser(), actorName));
        }
        return actors;
    }

    public static Set<Identifiable> getGroups(ScriptExecutionContext context, Set<String> groupNames) {
        Set<Identifiable> groups = Sets.newHashSet();
        for (String groupName : groupNames) {
            groups.add(context.getExecutorLogic().getGroup(context.getUser(), groupName));
        }
        return groups;
    }

    public static Set<Identifiable> getRelations(ScriptExecutionContext context, Set<String> relationNames) {
        Set<Identifiable> relations = Sets.newHashSet();
        for (String groupName : relationNames) {
            relations.add(context.getRelationLogic().getRelation(context.getUser(), groupName));
        }
        return relations;
    }

    public static Set<Identifiable> getProcessDefinitions(ScriptExecutionContext context, Set<String> processDefinitionNames) {
        Set<Identifiable> processDefinitions = Sets.newHashSet();
        for (String definitionName : processDefinitionNames) {
            processDefinitions.add(context.getDefinitionLogic().getLatestProcessDefinition(context.getUser(), definitionName));
        }
        return processDefinitions;
    }

    public static Set<Identifiable> getProcesses(ScriptExecutionContext context, Set<String> processDefinitionNames) {
        Set<Identifiable> processInstances = Sets.newHashSet();
        for (String definitionName : processDefinitionNames) {
            BatchPresentation batchPresentation = BatchPresentationFactory.PROCESSES.createNonPaged();
            int definitionNameIndex = batchPresentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.DEFINITION_NAME);
            batchPresentation.getFilteredFields().put(definitionNameIndex, new StringFilterCriteria(definitionName));
            processInstances.addAll(context.getExecutionLogic().getProcesses(context.getUser(), batchPresentation));
        }
        return processInstances;
    }

    public static Set<Identifiable> getReports(ScriptExecutionContext context, Set<String> reportNames) {
        Set<Identifiable> reports = Sets.newHashSet();
        for (String reportName : reportNames) {
            reports.add(context.getReportLogic().getReportDefinition(context.getUser(), reportName));
        }
        return reports;
    }
}
