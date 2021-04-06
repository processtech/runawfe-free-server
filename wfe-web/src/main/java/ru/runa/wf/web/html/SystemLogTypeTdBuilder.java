package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.presentation.SystemLogTypeHelper;

/**
 * {@link TdBuilder} implementation to show system log type in human readable
 * format.
 */
public class SystemLogTypeTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD result = new TD(getValue(object, env));
        result.setClass(Resources.CLASS_LIST_TABLE_TD);
        return result;
    }

    @Override
    public String getValue(Object object, Env env) {
        String displayProperty = SystemLogTypeHelper.getClasses().get(object.getClass());
        if (displayProperty != null) {
            return Messages.getMessage(displayProperty, env.getPageContext());
        } else {
            return MessagesOther.SYSTEM_LOG_UNDEFINED_TYPE.message(env.getPageContext());
        }
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
