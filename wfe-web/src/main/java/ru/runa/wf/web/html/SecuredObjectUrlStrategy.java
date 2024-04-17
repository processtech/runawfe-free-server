package ru.runa.wf.web.html;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.SecuredObject;

public class SecuredObjectUrlStrategy implements ItemUrlStrategy {
    private final PageContext pageContext;

    public SecuredObjectUrlStrategy(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public String getUrl(String baseUrl, Object item) {
        Long definitionId = ((SecuredObject) item).getSecuredObjectId();
        Map<String, Object> map = Maps.newHashMap();
        map.put(IdForm.ID_INPUT_NAME, definitionId);
        return Commons.getActionUrl(baseUrl, map, pageContext, PortletUrlType.Action);
    }
}
