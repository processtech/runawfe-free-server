package ru.runa.af.web.html;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.relation.RelationPair;

public class RelationGroupNameTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        td.addElement(new StringElement(getValue(object, env)));
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
        String name = ((RelationPair) object).getRelation().getName();
        return name == null ? "" : name;
    }
}
