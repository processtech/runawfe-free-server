package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.wfe.definition.dto.WfProcessDefinitionChange;


public class DefinitionChangesCommentTDBuilder implements TDBuilder{

    public DefinitionChangesCommentTDBuilder() {
    }

    @Override
    public TD build(Object object, TDBuilder.Env env) {
        WfProcessDefinitionChange change = (WfProcessDefinitionChange) object;
        TD td = new TD(change.getComment());
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, TDBuilder.Env env) {
        WfProcessDefinitionChange pdc = (WfProcessDefinitionChange) object;
        return pdc.getComment();
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
