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
        super(Permission.LIST);
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
