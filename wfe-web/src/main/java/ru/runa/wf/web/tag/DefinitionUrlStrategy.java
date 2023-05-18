package ru.runa.wf.web.tag;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.dto.WfDefinition;

import com.google.common.collect.Maps;

public class DefinitionUrlStrategy implements ItemUrlStrategy {
    private final PageContext pageContext;

    public DefinitionUrlStrategy(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public String getUrl(String baseUrl, Object item) {
        WfDefinition definition = (WfDefinition) item;
        Long definitionId = definition.getId();
        Map<String, Object> map = Maps.newHashMap();
        map.put(IdForm.ID_INPUT_NAME, definitionId);
        return Commons.getActionUrl(baseUrl, map, pageContext, PortletUrlType.Action);
    }
}