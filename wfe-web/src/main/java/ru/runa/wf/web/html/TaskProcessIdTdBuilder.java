package ru.runa.wf.web.html;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Map;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TdBuilder.Env.SecuredObjectExtractor;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Created on 14.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class TaskProcessIdTdBuilder implements TdBuilder, Serializable {
    private static final long serialVersionUID = 1L;

    public TaskProcessIdTdBuilder() {
    }

    @Override
    public TD build(Object object, Env env) {
        WfTask task = (WfTask) object;
        Long processId = task.getProcessId();
        boolean isAllowed;
        try {
            isAllowed = env.isAllowed(Permission.READ, new SecuredObjectExtractor() {
                private static final long serialVersionUID = 1L;

                @Override
                public SecuredObject getSecuredObject(Object o, Env env) {
                    throw new IllegalAccessError();
                }

                @Override
                public SecuredObjectType getSecuredObjectType(Object o, Env env) {
                    return SecuredObjectType.PROCESS;
                }

                @Override
                public Long getSecuredObjectId(Object o, Env env) {
                    return ((WfTask) o).getProcessId();
                }
            });
        } catch (Exception e) {
            isAllowed = false;
        }

        ConcreteElement link = new StringElement(processId.toString());
        if (isAllowed) {
            Map<String, Object> params = Maps.newHashMap();
            if (task.getProcessId() == null) {
                params.put(IdForm.ID_INPUT_NAME, processId);
                params.put(TaskIdForm.TASK_ID_INPUT_NAME, task.getId());
            } else {
                Long rootProcessId = ProcessHierarchyUtils.getRootProcessId(task.getProcessHierarchyIds());
                link = new StringElement((rootProcessId == null ? task.getProcessId() : rootProcessId).toString());
                params.put(IdForm.ID_INPUT_NAME, link);
                params.put(TaskIdForm.TASK_ID_INPUT_NAME, task.getId());
            }
            String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, env.getPageContext(), PortletUrlType.Render);
            link = new A(url, link);
        }

        TD td = new TD(link);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfTask task = (WfTask) object;
        return task.getProcessId().toString();
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
