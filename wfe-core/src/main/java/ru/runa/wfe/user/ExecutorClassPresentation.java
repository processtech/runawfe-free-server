package ru.runa.wfe.user;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

/**
 * Created on 22.10.2005
 */
public class ExecutorClassPresentation extends ClassPresentation {
    public static final String NAME = "name";
    public static final String FULL_NAME = "fullName";
    public static final String DESCRIPTION = "description";
    public static final String TYPE = "type";
    public static final String CODE = "code";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String DEPARTAMENT = "department";
    public static final String TITLE = "title";

    public static final ClassPresentation INSTANCE = new ExecutorClassPresentation();

    private ExecutorClassPresentation() {
        super(Executor.class, "", true, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode get
                // value/show in web getter param
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(Executor.class, "name"), true, 1, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "name" }),
                new FieldDescriptor(FULL_NAME, String.class.getName(), new DefaultDbSource(Executor.class, "fullName"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "fullName" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(Executor.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "description" }),
                new FieldDescriptor(CODE, String.class.getName(), new DefaultDbSource(Executor.class, "code"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "code" }).setVisible(false),
                new FieldDescriptor(EMAIL, String.class.getName(), new DefaultDbSource(Executor.class, "email"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "email" }).setVisible(false),
                new FieldDescriptor(PHONE, String.class.getName(), new DefaultDbSource(Executor.class, "phone"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "phone" }).setVisible(false),
                new FieldDescriptor(DEPARTAMENT, String.class.getName(), new DefaultDbSource(Executor.class, "department"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "department" }).setVisible(false),
                new FieldDescriptor(TITLE, String.class.getName(), new DefaultDbSource(Executor.class, "title"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "title" }).setVisible(false),
                new FieldDescriptor(TYPE, String.class.getName(), new DefaultDbSource(Executor.class, "class"), false, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "class" }).setShowable(false) });
    }
}
