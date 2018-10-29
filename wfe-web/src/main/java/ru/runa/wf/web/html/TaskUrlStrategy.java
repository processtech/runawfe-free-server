package ru.runa.wf.web.html;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.collect.Maps;

public class TaskUrlStrategy implements ItemUrlStrategy {
    private final PageContext pageContext;

    public TaskUrlStrategy(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public String getUrl(String baseUrl, Object item) {
        WfTask task = (WfTask) item;
        Map<String, Object> map = Maps.newHashMap();
        map.put(IdForm.ID_INPUT_NAME, task.getId());
        map.put(ProcessForm.ACTOR_ID_INPUT_NAME, task.getTargetActor().getId());
        return Commons.getActionUrl(baseUrl, map, pageContext, PortletUrlType.Action);
    }
}
