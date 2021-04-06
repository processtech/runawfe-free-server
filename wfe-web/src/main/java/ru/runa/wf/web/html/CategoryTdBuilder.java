package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.EntityWithType;

/**
 * Created on 10.08.2007
 * 
 * @author kana
 */
public class CategoryTdBuilder implements TdBuilder {

    public CategoryTdBuilder() {
    }

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        String type = getValue(object, env);
        td.addElement(type);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        EntityWithType pd = (EntityWithType) object;
        String result = pd.getCategories()[pd.getCategories().length - 1];
        if (result == null) {
            result = "";
        }
        return result;
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        EntityWithType pd = (EntityWithType) object;
        return pd.getCategories();
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        EntityWithType pd = (EntityWithType) object;
        return pd.getCategories().length;
    }
}
