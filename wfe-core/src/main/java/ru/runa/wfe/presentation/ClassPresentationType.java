package ru.runa.wfe.presentation;

import java.util.HashMap;
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

public enum ClassPresentationType {
    NONE(null, ""),
    SYSTEM_LOG(SystemLogClassPresentation.getInstance(), "system_log"),
    EXECUTOR(ExecutorClassPresentation.getInstance(), "executor"),
    ACTOR(ActorClassPresentation.getInstance(), ""),
    GROUP(GroupClassPresentation.getInstance(), "group"),
    RELATION(RelationClassPresentation.getInstance(), "relation"),
    RELATIONPAIR(RelationPairClassPresentation.getInstance(), "relationpair"),
    DEFINITION(DefinitionClassPresentation.getInstance(), "process_definition"),
    DEFINITION_HISTORY(DefinitionHistoryClassPresentation.getInstance(), ""),
    PROCESS(ProcessClassPresentation.getInstance(), "process"),
    PROCESS_WITH_TASKS(ProcessWithTasksClassPresentation.getInstance(), "process"),
    TASK(TaskClassPresentation.getInstance(), "task"),
    TASK_OBSERVABLE(TaskObservableClassPresentation.getInstance(), "task"),
    REPORTS(ReportClassPresentation.getInstance(), "report");

    private final Class<?> presentationClass;
    private final String restrictions;
    private final boolean withPaging;
    private final FieldDescriptor[] fields;
    private final HashMap<String, Integer> fieldIndexesByName = new HashMap<>();
    private final String localizationKey;

    ClassPresentationType(ClassPresentation cp, String localizationKey) {
        if (cp != null) {
            presentationClass = cp.getPresentationClass();
            restrictions = cp.getRestrictions();
            withPaging = cp.isWithPaging();
            fields = cp.getFields();
            populateFieldIndexesByName();
        } else {
            presentationClass = null;
            restrictions = null;
            withPaging = false;
            fields = null;
        }
        this.localizationKey = localizationKey;
    }

    private void populateFieldIndexesByName() {
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                fieldIndexesByName.put(fields[i].name, i);
            }
        }
    }

    public Class<?> getPresentationClass() {
        return presentationClass;
    }

    public String getRestrictions() {
        return restrictions;
    }

    public boolean isWithPaging() {
        return withPaging;
    }

    public FieldDescriptor[] getFields() {
        return fields;
    }

    public int getFieldIndex(String name) {
        Integer result = fieldIndexesByName.get(name);
        if (result != null) {
            return result;
        } else {
            throw new InternalApplicationException("Field '" + name + "' is not found in " + this);
        }
    }

    public String getLocalizationKey() {
        return localizationKey;
    }
}
