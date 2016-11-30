package ru.runa.wf.web.tag;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.wfe.commons.web.PortletUrlType;

import com.google.common.collect.Maps;
import ru.runa.wfe.definition.dto.WfProcessDefinitionChange;

public class DefinitionChangesUrlStrategy implements ItemUrlStrategy {
    private final PageContext pageContext;

    public DefinitionChangesUrlStrategy(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public String getUrl(String baseUrl, Object item) {
        WfProcessDefinitionChange change = (WfProcessDefinitionChange) item;
        Long changeId = change.getId();
        Map<String, Object> map = Maps.newHashMap();
        map.put(IdForm.ID_INPUT_NAME, changeId);
        return Commons.getActionUrl(baseUrl, map, pageContext, PortletUrlType.Action);
    }
}