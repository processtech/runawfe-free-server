package ru.runa.wfe.report;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.security.Permission;

/**
 * Class presentation for reports.
 */
public class ReportClassPresentation extends ClassPresentation {
    private static final String PropertyTdBuilder = "ru.runa.common.web.html.PropertyTdBuilder";

    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CATEGORY = "category";

    public static final ClassPresentation INSTANCE = new ReportClassPresentation();

    private ReportClassPresentation() {
        super(ReportDefinition.class, null, false, new FieldDescriptor[] {
                new FieldDescriptor(NAME, AnywhereStringFilterCriteria.class.getName(), new DefaultDbSource(ReportDefinition.class, "name"), true, 1,
                        BatchPresentationConsts.ASC, FieldFilterMode.DATABASE, PropertyTdBuilder, new Object[] { Permission.READ, "name" }),
                new FieldDescriptor(DESCRIPTION, AnywhereStringFilterCriteria.class.getName(), new DefaultDbSource(ReportDefinition.class,
                        "description"), true, FieldFilterMode.DATABASE, PropertyTdBuilder, new Object[] { Permission.READ, "description" }),
                new FieldDescriptor(CATEGORY, AnywhereStringFilterCriteria.class.getName(), new DefaultDbSource(ReportDefinition.class, "category"),
                        true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.CategoryTdBuilder", new Object[] {}) });
    }
}
