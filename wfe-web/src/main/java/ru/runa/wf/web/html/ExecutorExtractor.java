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

import java.util.Map;
import java.util.WeakHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.common.web.html.TdBuilder.Env;
import ru.runa.common.web.html.TdBuilder.Env.SecuredObjectExtractor;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

/**
 * Creates {@link SecuredObject} to check permissions on {@link Actor}, executed
 * action.
 */
public class ExecutorExtractor extends SecuredObjectExtractor {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ExecutorExtractor.class);
    private final Map<Long, SecuredObject> cache = new WeakHashMap<>();

    @Override
    public SecuredObject getSecuredObject(Object o, Env env) {
        Long id = (Long) o;
        try {
            if (!cache.containsKey(id)) {
                cache.put(id, Delegates.getExecutorService().getExecutor(env.getUser(), id));
            }
            return cache.get(id);
        } catch (Exception e) {
            log.error("Can't load executor for system log with id " + id, e);
        }
        return null;
    }
}
