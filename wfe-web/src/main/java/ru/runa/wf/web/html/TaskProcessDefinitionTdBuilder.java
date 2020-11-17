package ru.runa.wf.web.html;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
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
        String definitionName = getValue(object, env);
        Long definitionVersionId = task.getDefinitionVersionId();
        Long rootProcessId = ProcessHierarchyUtils.getRootProcessId(task.getProcessHierarchyIds());
        if (!rootProcessId.equals(task.getProcessId())) {
            WfProcess wfp = Delegates.getExecutionService().getProcess(env.getUser(), rootProcessId);
            definitionName = wfp.getName();
            definitionVersionId = wfp.getDefinitionVersionId();
        }
        if (env.hasProcessDefinitionPermission(Permission.READ, definitionVersionId)) {
            String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_MANAGE_DEFINITION, IdForm.ID_INPUT_NAME, definitionVersionId,
                    env.getPageContext(), PortletUrlType.Render);
            A definitionNameLink = new A(url, definitionName);
            td.addElement(definitionNameLink);
        } else {
            // this should never happend, since read permission required to
            // get definition
            addDisabledDefinitionName(td, definitionName);
        }
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return ((WfTask) object).getDefinitionName();
    }

    private TD addDisabledDefinitionName(TD td, String name) {
        ConcreteElement nameElement = new StringElement(name);
        td.addElement(nameElement);
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
}
