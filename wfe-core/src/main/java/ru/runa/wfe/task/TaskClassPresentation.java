package ru.runa.wfe.task;

import java.util.Date;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.VariableDbSources;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.CurrentVariable;

/**
 * Created on 22.10.2005
 */
public class TaskClassPresentation extends ClassPresentation {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String DEFINITION_NAME = "definitionName";
    public static final String ROOT_DEFINITION_NAME = "rootDefinitionName";
    public static final String PROCESS_ID = "processId";
    public static final String ROOT_PROCESS_ID = "rootProcessId";
    public static final String OWNER = "owner";
    public static final String TASK_SWIMLINE = "swimlaneName";
    public static final String TASK_VARIABLE = "variable";
    public static final String TASK_DEADLINE = "deadlineDate";
    public static final String TASK_CREATE_DATE = "createDate";
    public static final String TASK_ASSIGN_DATE = "assignDate";
    public static final String TASK_DURATION = "duration";

    public static final ClassPresentation INSTANCE = new TaskClassPresentation();

    private TaskClassPresentation() {
        super(Task.class, "", false, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode
                // get value/show in web getter parameters
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(Task.class, "name"), true, 3, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "name" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(Task.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskDescriptionTdBuilder", new Object[] {}),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDbSource(Task.class, "process.definitionVersion.definition.name"),
                        true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskProcessDefinitionTdBuilder", new Object[] {}),
                new FieldDescriptor(ROOT_DEFINITION_NAME, String.class.getName(), new DefaultDbSource(Task.class, "process.definitionVersion.definition.name"), false,
                        FieldFilterMode.NONE, "ru.runa.wf.web.html.TaskRootProcessDefinitionTdBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDbSource(Task.class, "process.id"), true, 2,
                        BatchPresentationConsts.ASC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskProcessIdTdBuilder", new Object[] {}),
                new FieldDescriptor(ROOT_PROCESS_ID, Integer.class.getName(), new DefaultDbSource(Task.class, "process.id"), false,
                        FieldFilterMode.NONE, "ru.runa.wf.web.html.TaskRootProcessIdTdBuilder", new Object[] {}),
                new FieldDescriptor(OWNER, String.class.getName(), new DefaultDbSource(Task.class, "executor.name"), true,
                        FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.TaskOwnerTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_SWIMLINE, String.class.getName(), new DefaultDbSource(Task.class, "swimlane.name"), false,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskRoleTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_VARIABLE, CurrentVariable.class.getName(), VariableDbSources.get("process"), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.TaskVariableTdBuilder", new Object[] {}).setVariablePrototype(true),
                new FieldDescriptor(TASK_DEADLINE, Date.class.getName(), new DefaultDbSource(Task.class, "deadlineDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskDeadlineTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_CREATE_DATE, Date.class.getName(), new DefaultDbSource(Task.class, "createDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskCreationDateTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_ASSIGN_DATE, Date.class.getName(), new DefaultDbSource(Task.class, null), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.TaskAssignmentDateTdBuilder", new Object[] {}).setVisible(false),
                new FieldDescriptor(TASK_DURATION, String.class.getName(), new DefaultDbSource(Task.class, null), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.TaskDurationTdBuilder", new Object[] {}).setVisible(false) });

    }
}
