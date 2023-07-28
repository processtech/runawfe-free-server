package ru.runa.wfe.execution;

import java.util.Date;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.VariableDbSources;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.ArchivedVariable;

public class ArchivedProcessClassPresentation extends ClassPresentation {
    public static final String PROCESS_ID = "id";
    public static final String DEFINITION_NAME = "definitionName";
    public static final String PROCESS_START_DATE = "startDate";
    public static final String PROCESS_END_DATE = "endDate";
    public static final String DEFINITION_VERSION = "definitionVersion";
    public static final String PROCESS_VARIABLE = "variable";

    public static final ClassPresentation INSTANCE = new ArchivedProcessClassPresentation();

    private ArchivedProcessClassPresentation() {
        super(ArchivedProcess.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDbSource(ArchivedProcess.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "id" }),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDbSource(ArchivedProcess.class, "definition.pack.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "name" }),
                new FieldDescriptor(PROCESS_START_DATE, Date.class.getName(), new DefaultDbSource(ArchivedProcess.class, "startDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessStartDateTdBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_END_DATE, Date.class.getName(), new DefaultDbSource(ArchivedProcess.class, "endDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessEndDateTdBuilder", new Object[] {}),
                new FieldDescriptor(DEFINITION_VERSION, Integer.class.getName(), new DefaultDbSource(ArchivedProcess.class, "definition.version"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "version" }),
                new FieldDescriptor(PROCESS_ID, String.class.getName(), new SubProcessDbSource(ArchivedProcess.class,
                        "hierarchyIds"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.RootProcessTdBuilder", new Object[] {})
                        .setGroupableByProcessId(true),
                new FieldDescriptor(PROCESS_VARIABLE, ArchivedVariable.class.getName(), VariableDbSources.get(null), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.ProcessVariableTdBuilder", new Object[] {}).setVariablePrototype(true)
        });
    }
}
