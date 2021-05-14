package ru.runa.wfe.presentation;

import java.util.HashMap;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.SystemLogClassPresentation;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionHistoryClassPresentation;
import ru.runa.wfe.execution.ArchivedProcessClassPresentation;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.execution.CurrentProcessWithTasksClassPresentation;
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
    SYSTEM_LOG(SystemLogClassPresentation.INSTANCE, "system_log"),
    EXECUTOR(ExecutorClassPresentation.INSTANCE, "executor"),
    ACTOR(ActorClassPresentation.INSTANCE, ""),
    GROUP(GroupClassPresentation.INSTANCE, "group"),
    RELATION(RelationClassPresentation.INSTANCE, "relation"),
    RELATIONPAIR(RelationPairClassPresentation.INSTANCE, "relationpair"),
    DEFINITION(DefinitionClassPresentation.INSTANCE, "process_definition"),
    DEFINITION_HISTORY(DefinitionHistoryClassPresentation.INSTANCE, ""),
    ARCHIVED_PROCESS(ArchivedProcessClassPresentation.INSTANCE, "process"),
    CURRENT_PROCESS(CurrentProcessClassPresentation.INSTANCE, "process"),
    CURRENT_PROCESS_WITH_TASKS(CurrentProcessWithTasksClassPresentation.INSTANCE, "process"),
    TASK(TaskClassPresentation.INSTANCE, "task"),
    TASK_OBSERVABLE(TaskObservableClassPresentation.INSTANCE, "task"),
    REPORTS(ReportClassPresentation.INSTANCE, "report");

    private final Class<?> presentationClass;
    private final String restrictions;
    private final boolean withPaging;
    private final FieldDescriptor[] fields;
    private final HashMap<String, Integer> fieldIndexesByName = new HashMap<>();
    private final String localizationKey;
    private int variablePrototypeIndex = -1;

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
                if (fields[i].variablePrototype) {
                    variablePrototypeIndex = i;
                }
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

    public int getVariablePrototypeIndex() {
        return variablePrototypeIndex;
    }

}
