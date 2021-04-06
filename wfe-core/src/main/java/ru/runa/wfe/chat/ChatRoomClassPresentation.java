package ru.runa.wfe.chat;

import java.util.Date;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.VariableDbSources;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.Variable;

import static ru.runa.wfe.execution.CurrentProcessClassPresentation.DEFINITION_NAME;
import static ru.runa.wfe.execution.CurrentProcessClassPresentation.DEFINITION_VERSION;
import static ru.runa.wfe.execution.CurrentProcessClassPresentation.ERRORS;
import static ru.runa.wfe.execution.CurrentProcessClassPresentation.PROCESS_END_DATE;
import static ru.runa.wfe.execution.CurrentProcessClassPresentation.PROCESS_ID;
import static ru.runa.wfe.execution.CurrentProcessClassPresentation.PROCESS_START_DATE;
import static ru.runa.wfe.execution.CurrentProcessClassPresentation.PROCESS_VARIABLE;

/**
 * Created on 21.02.2021
 *
 * @author Sergey Inyakin
 */
public class ChatRoomClassPresentation extends ClassPresentation {
    public static final String NEW_MESSAGES = "chat_rooms.new_messages";
    private static final ClassPresentation INSTANCE = new ChatRoomClassPresentation();

    private ChatRoomClassPresentation() {
        super(ChatRoom.class, "", true, new FieldDescriptor[]{
                new FieldDescriptor(NEW_MESSAGES, Long.class.getName(), new DefaultDbSource(ChatRoom.class, "newMessagesCount"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ChatNewMessagesCountTdBuilder", new Object[]{ Permission.READ, "processId" }),
                new FieldDescriptor(PROCESS_ID, Long.class.getName(), new DefaultDbSource(ChatRoom.class, "id"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{ Permission.READ, "id" }),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDbSource(ChatRoom.class, "definitionVersion.definition.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{ Permission.READ, "process.name" }),
                new FieldDescriptor(PROCESS_START_DATE, Date.class.getName(), new DefaultDbSource(ChatRoom.class, "process.startDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ChatRoomStartDateTdBuilder", new Object[]{}).setVisible(false),
                new FieldDescriptor(PROCESS_END_DATE, Date.class.getName(), new DefaultDbSource(ChatRoom.class, "process.endDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ChatRoomEndDateTdBuilder", new Object[]{}).setVisible(false),
                new FieldDescriptor(PROCESS_VARIABLE, Variable.class.getName(), VariableDbSources.get(null), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ChatRoomVariableTdBuilder", new Object[]{}),
                new FieldDescriptor(DEFINITION_VERSION, Integer.class.getName(), new DefaultDbSource(ChatRoom.class, "definitionVersion.version"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{ Permission.READ, "process.version" }).setVisible(false),
                new FieldDescriptor(ERRORS, String.class.getName(), new DefaultDbSource(Token.class, "errorMessage"), false,
                        FieldFilterMode.NONE, "ru.runa.wf.web.html.ChatRoomErrorsTdBuilder", new Object[]{}).setVisible(false) });
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
