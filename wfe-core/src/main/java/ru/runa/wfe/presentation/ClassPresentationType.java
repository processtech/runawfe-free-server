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
    NONE(null),
    SYSTEM_LOG(SystemLogClassPresentation.INSTANCE),
    EXECUTOR(ExecutorClassPresentation.INSTANCE),
    ACTOR(ActorClassPresentation.INSTANCE),
    GROUP(GroupClassPresentation.INSTANCE),
    RELATION(RelationClassPresentation.INSTANCE),
    RELATIONPAIR(RelationPairClassPresentation.INSTANCE),
    DEFINITION(DefinitionClassPresentation.INSTANCE),
    DEFINITION_HISTORY(DefinitionHistoryClassPresentation.INSTANCE),
    ARCHIVED_PROCESS(ArchivedProcessClassPresentation.INSTANCE),
    CURRENT_PROCESS(CurrentProcessClassPresentation.INSTANCE),
    CURRENT_PROCESS_WITH_TASKS(CurrentProcessWithTasksClassPresentation.INSTANCE),
    TASK(TaskClassPresentation.INSTANCE),
    TASK_OBSERVABLE(TaskObservableClassPresentation.INSTANCE),
    REPORTS(ReportClassPresentation.INSTANCE);

    private final Class<?> presentationClass;
    private final String restrictions;
    private final boolean withPaging;
    private final FieldDescriptor[] fields;
    private final HashMap<String, Integer> fieldIndexesByName = new HashMap<>();

    // TODO dimgel wants to transform ClassPresentation class hierarchy to enum
//    ClassPresentationType(Class<?> presentationClass, String restrictions, boolean withPaging, FieldDescriptor[] fields) {
//        this.presentationClass = presentationClass;
//        this.restrictions = restrictions;
//        this.withPaging = withPaging;
//        this.fields = fields;
//        populateFieldIndexesByName();
//    }

//    @Deprecated
    ClassPresentationType(ClassPresentation cp) {
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
    }

    private void populateFieldIndexesByName() {
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                fieldIndexesByName.put(fields[i].displayName, i);
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
}
