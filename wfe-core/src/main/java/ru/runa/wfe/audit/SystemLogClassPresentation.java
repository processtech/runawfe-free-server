/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.audit;

import java.util.Date;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
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
    public static final String LOG_ID = "batch_presentation.system_log.id";

    /**
     * System log actor.
     */
    public static final String ACTOR = "batch_presentation.system_log.actor";

    /**
     * System log time.
     */
    public static final String TIME = "batch_presentation.system_log.time";

    /**
     * System log type.
     */
    public static final String TYPE = "batch_presentation.system_log.type";

    /**
     * System log message.
     */
    public static final String MESSAGE = "batch_presentation.system_log.message";

    private static final ClassPresentation INSTANCE = new SystemLogClassPresentation();

    /**
     * Data source to sort and filter by executor name.
     */
    static class ActorDBSource extends DefaultDBSource {
        public ActorDBSource() {
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
                new FieldDescriptor(LOG_ID, Integer.class.getName(), new DefaultDBSource(SystemLog.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "id", true }),
                new FieldDescriptor(TIME, Date.class.getName(), new DefaultDBSource(SystemLog.class, "createDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] {
                                new Permission(), "createDate", true }),
                new FieldDescriptor(ACTOR, String.class.getName(), new ActorDBSource(), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.SystemLogActorTDBuilder", new Object[] {}),
                new FieldDescriptor(TYPE, SystemLogTypeFilterCriteria.class.getName(), new DefaultDBSource(SystemLog.class, "class"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.SystemLogTypeTDBuilder", new Object[] {}),
                new FieldDescriptor(MESSAGE, String.class.getName(), new DefaultDBSource(SystemLog.class, "id"), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.SystemLogTDBuilder", new Object[] {}) });
    }

    /**
     * Returns instance of {@link SystemLogClassPresentation}.
     *
     * @return Instance of {@link SystemLogClassPresentation}.
     */
    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
