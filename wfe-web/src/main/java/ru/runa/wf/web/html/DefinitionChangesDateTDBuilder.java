package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.TDBuilder;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.dto.WfProcessDefinitionChange;


public class DefinitionChangesDateTDBuilder implements TDBuilder {
    public DefinitionChangesDateTDBuilder() {
    }

    @Override
    public TD build(Object object, TDBuilder.Env env) {
        WfProcessDefinitionChange change = (WfProcessDefinitionChange) object;
        TD td = new TD(CalendarUtil.format(change.getDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT));
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, TDBuilder.Env env) {
        WfProcessDefinitionChange pdc = (WfProcessDefinitionChange) object;
        return CalendarUtil.format(pdc.getDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
    }

    @Override
    public String[] getSeparatedValues(Object object, TDBuilder.Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, TDBuilder.Env env) {
        return 1;
    }
}