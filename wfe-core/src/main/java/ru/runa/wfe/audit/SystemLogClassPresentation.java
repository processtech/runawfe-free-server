package ru.runa.wfe.audit;

import java.util.Date;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.SystemLogTypeFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;

/**
 * Presentation class to access {@link SystemLog} objects.
 */
public class SystemLogClassPresentation extends ClassPresentation {
    /**
     * System log identity.
     */
    public static final String LOG_ID = "id";

    /**
     * System log actor.
     */
    public static final String ACTOR = "actor";

    /**
     * System log time.
     */
    public static final String CREATE_DATE = "createDate";

    /**
     * System log type.
     */
    public static final String TYPE = "type";

    /**
     * System log message.
     */
    public static final String MESSAGE = "message";

    public static final ClassPresentation INSTANCE = new SystemLogClassPresentation();

    /**
     * Data source to sort and filter by executor name.
     */
    static class ActorDbSource extends DefaultDbSource {
        public ActorDbSource() {
            super(Executor.class, "name");
        }

        @Override
        public String getJoinExpression(String alias) {
            return classNameSQL + ".actorId=" + alias + ".id";
        }
    }

    /**
     * Creates instance of presentation class to access {@link SystemLog} objects.
     */
    private SystemLogClassPresentation() {
        super(SystemLog.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(LOG_ID, Integer.class.getName(), new DefaultDbSource(SystemLog.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "id", true }),
                new FieldDescriptor(CREATE_DATE, Date.class.getName(), new DefaultDbSource(SystemLog.class, "createDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] {
                        Permission.NONE, "createDate", true }),
                new FieldDescriptor(ACTOR, String.class.getName(), new ActorDbSource(), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.SystemLogActorTdBuilder", new Object[] {}),
                new FieldDescriptor(TYPE, SystemLogTypeFilterCriteria.class.getName(), new DefaultDbSource(SystemLog.class, "class"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.SystemLogTypeTdBuilder", new Object[] {}),
                new FieldDescriptor(MESSAGE, String.class.getName(), new DefaultDbSource(SystemLog.class, "id"), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.SystemLogTdBuilder", new Object[] {}) });
    }
}
