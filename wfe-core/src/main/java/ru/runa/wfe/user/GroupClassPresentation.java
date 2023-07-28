package ru.runa.wfe.user;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

/**
 * @author dofs
 */
public class GroupClassPresentation extends ClassPresentation {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";

    public static final ClassPresentation INSTANCE = new GroupClassPresentation();

    private GroupClassPresentation() {
        super(Group.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(Group.class, "name"), true, 1, BatchPresentationConsts.ASC,
                		FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "name" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(Group.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "description" }) });
    }
}
