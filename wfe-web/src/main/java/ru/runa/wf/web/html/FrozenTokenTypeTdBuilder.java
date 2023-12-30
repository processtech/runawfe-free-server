package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.execution.dto.WfFrozenToken;

public class FrozenTokenTypeTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfFrozenToken frozenToken = (WfFrozenToken) object;
        if (frozenToken.getNodeType() == null) {
            return "";
        }
        if (env.getPageContext() != null) {
            return Messages.getMessage(frozenToken.getTypeName(), env.getPageContext());
        }
        return frozenToken.getTypeName();
    }
}
