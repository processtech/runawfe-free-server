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

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;

/**
 * Builds table cell for {@link Executor}
 */
public abstract class BaseExecutorTdBuilder<T> extends BaseTdBuilder {

    public BaseExecutorTdBuilder() {
        super(Permission.READ);
    }

    protected abstract Executor getExecutor(T object, Env env);

    @Override
    public TD build(Object object, Env env) {
        ConcreteElement element;
        Executor executor = getExecutor((T) object, env);
        if (executor == null || !isEnabled(object, env)) {
            element = new StringElement(getValue(object, env));
        } else {
            String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, executor.getId(),
                    env.getPageContext(), PortletUrlType.Action);
            element = new A(url, getValue(object, env));
        }
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(element);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return HTMLUtils.getExecutorName(getExecutor((T) object, env), env.getPageContext());
    }
}
