package ru.runa.wfe.execution;

import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

public class TokenClassPresentation extends ClassPresentation {
    public static final String TOKEN_ID = "tokenId";
    public static final String NODE_ID = "nodeId";
    public static final String NODE_NAME = "nodeName";
    public static final String EXECUTION_STATUS = "executionStatus";
    public static final String ERROR_MESSAGE = "errorMessage";

    public static final ClassPresentation INSTANCE = new TokenClassPresentation();

    private TokenClassPresentation() {
        super(Token.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(TOKEN_ID, Long.class.getName(), new DefaultDbSource(Token.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "id", true }),
                new FieldDescriptor(NODE_ID, String.class.getName(), new DefaultDbSource(Token.class, "nodeId"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "node.id", true }),
                new FieldDescriptor(NODE_NAME, String.class.getName(), new DefaultDbSource(Token.class, "nodeName"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "node.name", true }),
                new FieldDescriptor(EXECUTION_STATUS, String.class.getName(), new DefaultDbSource(Token.class, "executionStatus"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TokenExecutionStatusTdBuilder", new Object[] {}),
                new FieldDescriptor(ERROR_MESSAGE, String.class.getName(), new DefaultDbSource(Token.class, "errorMessage"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TokenErrorMessageTdBuilder", new Object[] {})
        });
    }

}
