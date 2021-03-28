package ru.runa.wfe.chat;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;
import java.util.Date;

/**
 * Created on 21.02.2021
 *
 * @author Sergey Inyakin
 */
public class ChatRoomClassPresentation extends ClassPresentation {
    public static final String PROCESS_ID = "batch_presentation.process.id";
    public static final String DEFINITION_NAME = "batch_presentation.process.definition_name";
    public static final String NEW_MESSAGES = "chat_rooms.new_messages";
    public static final String PROCESS_START_DATE = "batch_presentation.process.started";
    public static final String PROCESS_END_DATE = "batch_presentation.process.ended";
    public static final String DEFINITION_VERSION = "batch_presentation.process.definition_version";

    private static final ClassPresentation INSTANCE = new ChatRoomClassPresentation();

    private ChatRoomClassPresentation() {
        super(Process.class, "", true, new FieldDescriptor[]{
                new FieldDescriptor(NEW_MESSAGES, Long.class.getName(), new ChatRoomDbSource(), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ChatNewMessagesCountTdBuilder", new Object[]{ Permission.READ, "processId" }),
                new FieldDescriptor(PROCESS_ID, Long.class.getName(), new DefaultDbSource(Process.class, "id"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{ Permission.READ, "id" }),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDbSource(Process.class, "deployment.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{ Permission.READ, "processName" }),
                new FieldDescriptor(PROCESS_START_DATE, Date.class.getName(), new DefaultDbSource(Process.class, "startDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ChatRoomStartDateTdBuilder", new Object[] {}).setVisible(false),
                new FieldDescriptor(PROCESS_END_DATE, Date.class.getName(), new DefaultDbSource(Process.class, "endDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ChatRoomEndDateTdBuilder", new Object[] {}).setVisible(false),
                new FieldDescriptor(DEFINITION_VERSION, Integer.class.getName(), new DefaultDbSource(Process.class, "deployment.version"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.READ, "version" }).setVisible(false) });
    }

    private static class ChatRoomDbSource extends DefaultDbSource {
        public ChatRoomDbSource() {
            super(UnreadMessagesPresentation.class, "numberOfUnreadMessages");
        }

        @Override
        public String getJoinExpression(String alias) {
            return classNameSQL + ".id=" + alias + ".process.id";
        }
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
