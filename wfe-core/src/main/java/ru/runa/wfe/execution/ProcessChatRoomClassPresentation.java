package ru.runa.wfe.execution;

import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

/**
 * Created on 21.02.2021
 *
 * @author Sergey Inyakin
 */
public class ProcessChatRoomClassPresentation extends ClassPresentation {
    public static final String PROCESS_ID = "batch_presentation.process.id";
    public static final String DEFINITION_NAME = "batch_presentation.process.definition_name";
    public static final String NEW_MESSAGES = "chat_rooms.new_messages";

    private static final ClassPresentation INSTANCE = new ProcessChatRoomClassPresentation();

    static class ChatMessageRecipientDbSource extends DefaultDbSource {
        public ChatMessageRecipientDbSource(Class<?> sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }
    }

    private ProcessChatRoomClassPresentation() {
        super(Process.class, "", true, new FieldDescriptor[]{
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDbSource(Process.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{Permission.READ, "id"}),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDbSource(Process.class, "deployment.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[]{Permission.READ, "processName"}),
                new FieldDescriptor(NEW_MESSAGES, Integer.class.getName(), new ChatMessageRecipientDbSource(ChatMessageRecipient.class, "readDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ChatRoomTdBuilder", new Object[]{Permission.READ, "processId"})});
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
