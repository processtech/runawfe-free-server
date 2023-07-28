package ru.runa.wf.web.html;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wf.web.action.LoadProcessDefinitionHtmlFileAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.dto.WfDefinition;

/**
 * Created on 14.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class DescriptionProcessTdBuilder implements TdBuilder {

    public DescriptionProcessTdBuilder() {
    }

    @Override
    public TD build(Object object, Env env) {
        WfDefinition definition = (WfDefinition) object;
        ConcreteElement descriptionLink;
        if (definition.hasHtmlDescription()) {
            String url = Commons.getActionUrl(LoadProcessDefinitionHtmlFileAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getId(),
                    env.getPageContext(), PortletUrlType.Render);
            descriptionLink = new A(url, definition.getDescription());
        } else {
            descriptionLink = new StringElement(definition.getDescription());
        }
        TD td = new TD(descriptionLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfDefinition pd = (WfDefinition) object;
        String result = pd.getDescription();
        if (result == null) {
            result = "";
        }
        return result;
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
