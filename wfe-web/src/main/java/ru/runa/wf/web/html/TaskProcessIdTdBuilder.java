package ru.runa.wf.web.html;

import com.google.common.collect.ImmutableMap;
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
        Long processId = getProcessId(task);
        ConcreteElement link = new StringElement(processId.toString());
        if (isAllowed(env, processId)) {
            Map<String, Object> params = ImmutableMap.of(
                    IdForm.ID_INPUT_NAME, link,
                    TaskIdForm.SELECTED_TASK_PROCESS_ID_NAME, processId,
                    TaskIdForm.TASK_ID_INPUT_NAME, task.getId());
            String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, env.getPageContext(), PortletUrlType.Render);
            link = new A(url, link);
        }
        TD td = new TD(link);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    protected Long getProcessId(WfTask task) {
        return task.getProcessId();
    }

    private boolean isAllowed(Env env, Long processId) {
        try {
            return env.isAllowed(Permission.READ, new SecuredObjectExtractor() {
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
        } catch (Exception ignored) {
        }
        return false;
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
