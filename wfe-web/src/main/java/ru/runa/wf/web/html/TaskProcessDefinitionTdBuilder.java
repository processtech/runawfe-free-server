package ru.runa.wf.web.html;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class TaskProcessDefinitionTdBuilder implements TdBuilder {

    public TaskProcessDefinitionTdBuilder() {
    }

    @Override
    public TD build(Object object, Env env) {
        WfTask task = (WfTask) object;
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        Long definitionId = getDefinitionId(task);
        String definitionName = getValue(object, env);
        if (env.hasProcessDefinitionPermission(Permission.READ, definitionId)) {
            td.addElement(new A(Commons.getActionUrl(WebResources.ACTION_MAPPING_MANAGE_DEFINITION, IdForm.ID_INPUT_NAME,
                    definitionId, env.getPageContext(), PortletUrlType.Render), definitionName));
        } else {
            td.addElement(new StringElement(definitionName));
        }
        return td;
    }

    protected Long getDefinitionId(WfTask task) {
        return task.getDefinitionVersionId();
    }

    @Override
    public String getValue(Object object, Env env) {
        return ((WfTask) object).getDefinitionName();
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
