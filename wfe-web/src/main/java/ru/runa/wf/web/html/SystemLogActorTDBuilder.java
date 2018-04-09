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
package ru.runa.wf.web.html;

import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.user.Executor;

/**
 * Builds table cell for {@link Executor}, executed action.
 */
public class SystemLogActorTDBuilder extends BaseExecutorTDBuilder<SystemLog> {

    /**
     * Creates component to build table cell for {@link Executor}, executed
     * action.
     */
    public SystemLogActorTDBuilder() {
        setSecuredObjectExtractor(new ExecutorExtractor());
    }

    @Override
    protected boolean isEnabled(Object object, Env env) {
        return true;
    }

    @Override
    protected Executor getExecutor(SystemLog object, Env env) {
        return (Executor) getExtractor().getSecuredObject(object.getActorId(), env);
    }

}
