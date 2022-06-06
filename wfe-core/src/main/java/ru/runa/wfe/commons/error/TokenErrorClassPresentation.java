package ru.runa.wfe.commons.error;

import java.util.Date;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

import static ru.runa.wfe.presentation.BatchPresentationConsts.DESC;

public class TokenErrorClassPresentation extends ClassPresentation {
    public static final String PROCESS_ID = "processId";
    public static final String PROCESS_NAME = "processName";
    public static final String PROCESS_VERSION = "processVersion";
    public static final String NODE_ID = "nodeId";
    public static final String NODE_NAME = "nodeName";
    public static final String NODE_TYPE = "nodeType";
    public static final String NODE_ENTER_DATE = "nodeEnterDate";
    public static final String ERROR_DATE = "errorDate";
    public static final String ERROR_MESSAGE = "errorMessage";

    private static final ClassPresentation INSTANCE = new TokenErrorClassPresentation();

    private TokenErrorClassPresentation() {
        super(
                CurrentToken.class,
                classNameSQL + ".errorDate is not null and " + classNameSQL + ".executionStatus = '" + ExecutionStatus.FAILED + "'",
                true,
                new FieldDescriptor[] {
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDbSource(CurrentToken.class, "process.id"), true, 1, DESC,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{Permission.READ, "processId"}),
                new FieldDescriptor(PROCESS_NAME, String.class.getName(), new DefaultDbSource(CurrentToken.class, "process.definitionVersion.definition.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{Permission.READ, "processName"}),
                new FieldDescriptor(PROCESS_VERSION, Integer.class.getName(), new DefaultDbSource(CurrentToken.class, "process.definitionVersion.version"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{Permission.READ, "processVersion"}),
                new FieldDescriptor(NODE_ID, String.class.getName(), new DefaultDbSource(CurrentToken.class, "nodeId"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{Permission.READ, "nodeId"}).setVisible(false),
                new FieldDescriptor(NODE_NAME, String.class.getName(), new DefaultDbSource(CurrentToken.class, "nodeName"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{Permission.READ, "nodeName"}),
                new FieldDescriptor(NODE_TYPE, String.class.getName(), new DefaultDbSource(CurrentToken.class, "nodeType"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TokenNodeTypeTdBuilder", new Object[]{}),
                new FieldDescriptor(NODE_ENTER_DATE, Date.class.getName(), new DefaultDbSource(CurrentToken.class, "nodeEnterDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TokenNodeEnterDateTdBuilder", new Object[]{}),
                new FieldDescriptor(ERROR_DATE, Date.class.getName(), new DefaultDbSource(CurrentToken.class, "errorDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TokenErrorDateTdBuilder", new Object[]{}),
                new FieldDescriptor(ERROR_MESSAGE, String.class.getName(), new DefaultDbSource(CurrentToken.class, "errorMessage"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TokenErrorMessageTdBuilder", new Object[] {})
        });
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
