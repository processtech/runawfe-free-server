package ru.runa.wfe.presentation;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.SystemLogClassPresentation;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionHistoryClassPresentation;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.ProcessWithTasksClassPresentation;
import ru.runa.wfe.relation.RelationClassPresentation;
import ru.runa.wfe.relation.RelationPairClassPresentation;
import ru.runa.wfe.report.ReportClassPresentation;
import ru.runa.wfe.task.TaskClassPresentation;
import ru.runa.wfe.task.TaskObservableClassPresentation;
import ru.runa.wfe.user.ActorClassPresentation;
import ru.runa.wfe.user.ExecutorClassPresentation;
import ru.runa.wfe.user.GroupClassPresentation;

public class ClassPresentations {
    private static final Map<ClassPresentationType, ClassPresentation> map = new HashMap<ClassPresentationType, ClassPresentation>();
    static {
        registerClassPresentation(ClassPresentationType.EXECUTOR, ExecutorClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.ACTOR, ActorClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.GROUP, GroupClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.DEFINITION, DefinitionClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.DEFINITION_HISTORY, DefinitionHistoryClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.PROCESS, ProcessClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.PROCESS_WITH_TASKS, ProcessWithTasksClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.TASK, TaskClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.TASK_OBSERVABLE, TaskObservableClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.RELATIONPAIR, RelationPairClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.RELATION, RelationClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.SYSTEM_LOG, SystemLogClassPresentation.getInstance());
        registerClassPresentation(ClassPresentationType.REPORTS, ReportClassPresentation.getInstance());
    }

    private static void registerClassPresentation(ClassPresentationType type, ClassPresentation classPresentation) {
        map.put(type, classPresentation);
    }

    public static ClassPresentation getClassPresentation(ClassPresentationType type) {
        ClassPresentation result = map.get(type);
        if (result == null) {
            throw new InternalApplicationException("Failed to found ClassPresentation for '" + type + "'");
        }
        return result;
    }

}
