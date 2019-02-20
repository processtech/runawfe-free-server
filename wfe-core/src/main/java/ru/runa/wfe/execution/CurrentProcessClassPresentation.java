package ru.runa.wfe.execution;

import java.util.Date;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.VariableDbSources;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.CurrentVariable;

public class CurrentProcessClassPresentation extends ClassPresentation {
    public static final String PROCESS_ID = "batch_presentation.process.id";
    public static final String DEFINITION_NAME = "batch_presentation.process.definition_name";
    public static final String PROCESS_START_DATE = "batch_presentation.process.started";
    public static final String PROCESS_END_DATE = "batch_presentation.process.ended";
    public static final String DEFINITION_VERSION = "batch_presentation.process.definition_version";
    public static final String PROCESS_EXECUTION_STATUS = "batch_presentation.process.execution_status";
    public static final String ERRORS = "batch_presentation.process.errors";
    public static final String PROCESS_VARIABLE = editable_prefix + "name:batch_presentation.process.variable";

    public static final ClassPresentation INSTANCE = new CurrentProcessClassPresentation();

    private CurrentProcessClassPresentation() {
        super(CurrentProcess.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDbSource(CurrentProcess.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "id" }),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDbSource(CurrentProcess.class, "definitionVersion.definition.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "name" }),
                new FieldDescriptor(PROCESS_START_DATE, Date.class.getName(), new DefaultDbSource(CurrentProcess.class, "startDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessStartDateTdBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_END_DATE, Date.class.getName(), new DefaultDbSource(CurrentProcess.class, "endDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessEndDateTdBuilder", new Object[] {}),
                new FieldDescriptor(DEFINITION_VERSION, Integer.class.getName(), new DefaultDbSource(CurrentProcess.class, "definitionVersion.version"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "version" }),
                new FieldDescriptor(filterable_prefix + "batch_presentation.process.id", String.class.getName(), new SubProcessDbSource(
                        CurrentProcess.class, "hierarchyIds"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.RootProcessTdBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_VARIABLE, CurrentVariable.class.getName(), VariableDbSources.get(null), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.ProcessVariableTdBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_EXECUTION_STATUS, String.class.getName(), new DefaultDbSource(CurrentProcess.class, "executionStatus"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessExecutionStatusTdBuilder", new Object[] {}),
                new FieldDescriptor(ERRORS, String.class.getName(), new DefaultDbSource(Token.class, "errorMessage"), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.ProcessErrorsTdBuilder", new Object[] {}).setVisible(false) });
    }
}
